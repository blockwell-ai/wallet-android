package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.qr.view.StaticArgumentView
import ai.blockwell.qrdemo.qr.view.SuggestionCreatedView
import ai.blockwell.qrdemo.qr.view.VotingArgumentView
import ai.blockwell.qrdemo.qr.view.WinningsView
import ai.blockwell.qrdemo.viewmodel.TxModel
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.DIRECTORY_PICTURES
import android.os.PersistableBundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_tx_success.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject


/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxSuccessActivity : AppCompatActivity() {
    val scope = MainScope()
    val model by viewModel<TxModel>()
    val votingModel by viewModel<VotingModel>()
    val client by inject<ApiClient>()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null

    var txStatus: TransactionStatusResponse? = null
    var qr: CreateQrResponse? = null
    var qrUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tx_success)

        savedInstanceState?.apply {
            try {
                qr = getParcelable("qr")
                qrUri = getParcelable("uri")
                txStatus = getParcelable("tx")
            } catch (e: Exception) {
                Log.e("TxSuccess", "Exception reading parcelable", e)
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
        txStatus?.let {
            outState.putParcelable("tx", it)
        }

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
                txStatus?.let { tx ->
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
        title = tx.method

        if (!tx.creator.isNullOrEmpty()) {
            val spannable = SpannableStringBuilder(getString(R.string.requested_tx, tx.creator))
            val bold = StyleSpan(Typeface.BOLD)
            spannable.setSpan(bold, 30, 30 + tx.creator.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            description.text = spannable
        } else {
            description.textResource = R.string.requested_tx_no_creator
        }
        function.text = tx.method
        contract.text = tx.address

        arguments.removeAllViews()

        val voting = tx.method == "vote" && tx.arguments.size == 2

        tx.arguments.mapIndexed { index, it ->
            val view: View = if (voting && index == 0) {
                VotingArgumentView(this, it, tx, votingModel)
            } else {
                StaticArgumentView(this, it)
            }

            arguments.addView(view)
        }

        val txId = tx.transactionId

        if (txId != null) {
            subscribeToUpdates(txId)
        } else {
            longToast(R.string.unknown_error)
        }
    }

    private fun subscribeToUpdates(txId: String) {
        job?.cancel()

        job = scope.launch {
            model.getTxStatus(txId).channel.consumeEach { tx ->
                txStatus = tx
                tx.transactionHash?.let { hash ->
                    etherscan.setText(R.string.view_on_etherscan)
                    etherscan.textColorResource = R.color.link
                    etherscan.setOnClickListener { _ ->
                        val webpage = Uri.parse(Etherscan.tx(tx.network, hash))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }

                if (tx.status == "completed") {
                    status.setText(R.string.confirmed)
                    status.textColorResource = R.color.success
                    progress.visibility = View.INVISIBLE

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
                } else if (tx.status == "error") {
                    alert(getString(R.string.transfer_failed) + tx.error).show()
                }
            }
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
