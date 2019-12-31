package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.qr.view.ConfirmationMessageView
import ai.blockwell.qrdemo.qr.view.QrStepView
import ai.blockwell.qrdemo.qr.view.SuggestionCreatedView
import ai.blockwell.qrdemo.qr.view.WinningsView
import ai.blockwell.qrdemo.viewmodel.TxModel
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.PersistableBundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.ajalt.timberkt.Timber
import kotlinx.android.synthetic.main.activity_tx_success.*
import kotlinx.coroutines.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.longToast
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject


/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxSuccessActivity : BaseActivity() {
    val model by viewModel<TxModel>()
    val votingModel by viewModel<VotingModel>()
    val client by inject<ApiClient>()

    var stepViews: List<QrStepView> = listOf()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null

    var txStatus: ArrayList<TransactionStatusResponse> = arrayListOf()
    var qr: CreateQrResponse? = null
    var qrUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tx_success)
        setSupportActionBar(toolbar)

        savedInstanceState?.apply {
            try {
                qr = getParcelable("qr")
                qrUri = getParcelable("uri")
                getParcelableArrayList<TransactionStatusResponse>("tx")?.let {
                    txStatus = it
                }
            } catch (e: Exception) {
                Timber.e(e) { "Exception reading parcelable" }
            }
        }

        update(intent.getParcelableExtra("tx"))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        qr?.let {
            outState.putParcelable("qr", it)
        }
        qrUri?.let {
            outState.putParcelable("uri", it)
        }
        outState.putParcelableArrayList("tx", txStatus)

        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
        job = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> {
                txStatus
                        .forEach { tx ->
                            tx.events?.filterNotNull()?.let { list ->
                                list.filter { it.event == "SuggestionCreated" }
                                        .forEach {
                                            // If request is cancelled, the result arrays are empty.
                                            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                                                showSuggestion(tx, it)
                                            } else {
                                                renderSuggestion(it, qr)
                                            }
                                        }
                            }
                        }
            }
            else -> {
                // Ignore all other requests.
            }
        }

    }

    private fun update(tx: TxResponse) {
        title = tx.title ?: tx.steps.last().method

        preview.removeAllViews()

        stepViews = tx.steps.mapIndexed { index, step ->
            val view = QrStepView(this, tx, step, votingModel)
            view.update(mapOf())
            view.backgroundResource = when (index) {
                0 -> R.drawable.bg_func1
                1 -> R.drawable.bg_func2
                2 -> R.drawable.bg_func2
                3 -> R.drawable.bg_func2
                else -> R.drawable.bg_func1
            }
            preview.addView(view)
            view
        }

        loadStatuses(tx)
    }

    private fun loadStatuses(code: TxResponse) {
        Timber.d { "Going in to load statuses" }

        job = scope.launch {
            for ((index, view) in stepViews.withIndex()) {
                Timber.d { "Loading status for id $index with ${view.step.transactionId}" }
                if (txStatus.size > index) {
                    view.updateStatus(txStatus[index])
                    Timber.d { "Already had cached status" }
                    continue
                }

                if (!isActive) {
                    Timber.d { "Coroutine not active, breaking" }
                    break
                }

                if (view.step.transactionId != null) {
                    do {
                        Timber.d { "Loading TX status" }
                        val result = model.getTxStatus(view.step.transactionId)
                        val value = result.fold({ tx ->
                            Timber.d { "Received a tx status: ${tx.status}" }
                            view.updateStatus(tx)
                            when {
                                tx.status == "completed" -> {
                                    tx.events
                                            ?.filterNotNull()
                                            ?.let { events ->
                                                events.filter { it.event == "ItemDropped" || it.event == "TokenWin" }
                                                        .let { list ->
                                                            if (list.isNotEmpty()) {
                                                                showWinnings(tx, list)
                                                            }
                                                        }

                                                events.filter { it.event == "SuggestionCreated" }
                                                        .forEach {
                                                            showSuggestion(tx, it)
                                                        }
                                            }

                                    if (index == stepViews.size - 1 && code.confirmationMessage != null) {
                                        showConfirmationMessage(code, tx, code.confirmationMessage)
                                    }

                                    tx
                                }
                                tx.status == "error" -> {
                                    showError(tx, tx.error)
                                    tx
                                }
                                else -> null
                            }
                        }, {
                            Timber.e(it) { "Exception retrieving transaction status" }
                            null
                        })

                        if (value == null) {
                            try {
                                delay(5000)
                            } catch (e: CancellationException) {
                                Timber.d(e) { "Delay cancelled" }
                            }
                        } else {
                            txStatus.add(value)
                        }
                    } while (isActive && value == null)
                } else {
                    longToast(R.string.unknown_error)
                }
            }
        }
    }

    private fun showError(tx: TransactionStatusResponse, error: TransactionError?) {
        if (error != null) {
            val gas = error.gasRequired
            if (error.code == "gas" && gas != null) {
                alert(error.message + " Send at least "
                        + gas.toDecimals(18)
                        + " ETH to your wallet and try again.")
                        .show()
            } else {
                alert(error.message).show()
            }
        } else {
            alert("Unknown error occurred. If this persists, contact us at blockwell@blockwell.ai.")
        }
    }

    private fun showWinnings(tx: TransactionStatusResponse, events: List<LogEvent>) {
        val view = WinningsView(this, client)
        view.update(tx.network!!, events)
        extras.addView(view)
        extras.visibility = View.VISIBLE
    }

    private fun showSuggestion(tx: TransactionStatusResponse, event: LogEvent) {
        val qrResponse = qr

        if (qrResponse != null) {
            if (qrUri != null) {
                renderSuggestion(event, qrResponse, qrUri)
            } else {
                filePermissions(event, qrResponse)
            }
        } else {
            scope.launch {
                val result = model.createVoteQr(tx.contractId!!, event.returnValues.getValue("suggestionId").toInt())
                result.fold({
                    qr = it
                    filePermissions(event, it)
                }, {
                    alert(getString(R.string.failed_to_create_qr) + it.message).show()
                    renderSuggestion(event)
                })
            }
        }
    }

    private fun showConfirmationMessage(code: TxResponse, tx: TransactionStatusResponse, message: String) {
        val view = ConfirmationMessageView(this)
        view.update(code, tx, message)
        extras.addView(view)
        extras.visibility = View.VISIBLE
    }

    private fun filePermissions(event: LogEvent, qr: CreateQrResponse) {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                alert(R.string.write_permission_explanation).show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        123)
            }
        } else {
            writeQrAndRenderSuggestion(event, qr)
        }
    }

    private fun writeQrAndRenderSuggestion(event: LogEvent, qr: CreateQrResponse) {
        scope.launch {
            val dir = getExternalFilesDir(DIRECTORY_DOWNLOADS)!!
            val uri = model.localBitmapUri(qr.image, dir)
            qrUri = uri
            renderSuggestion(event, qr, uri)
        }
    }

    private fun renderSuggestion(event: LogEvent, qr: CreateQrResponse? = null, bitmapUri: Uri? = null) {
        val view = SuggestionCreatedView(this)
        view.update(event, qr, bitmapUri)
        extras.addView(view)
        extras.visibility = View.VISIBLE
    }
}
