package ai.blockwell.qrdemo

import ai.blockwell.qrdemo.api.Etherscan
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ai.blockwell.qrdemo.api.TxResponse
import ai.blockwell.qrdemo.view.ArgumentView
import ai.blockwell.qrdemo.viewmodel.TxModel
import kotlinx.android.synthetic.main.activity_tx_success.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.textColorResource
import org.koin.android.architecture.ext.viewModel


/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxSuccessActivity : AppCompatActivity() {
    val scope = MainScope()
    val model by viewModel<TxModel>()

    // The parent job for all background work this activity subscribes to
    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tx_success)

        update(intent.getParcelableExtra("tx"))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun update(tx: TxResponse) {
        title = tx.method

        val spannable = SpannableStringBuilder(getString(R.string.requested_tx, tx.creator))
        val bold = StyleSpan(Typeface.BOLD)
        spannable.setSpan(bold, 30, 30 + tx.creator.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        description.text = spannable
        function.text = tx.method

        arguments.removeAllViews()
        tx.arguments.forEach {
            val view = ArgumentView(this)
            view.update(it)
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
            model.getTxStatus(txId).channel.consumeEach {
                it.transactionHash?.let { hash ->
                    etherscan.setText(R.string.view_on_etherscan)
                    etherscan.textColorResource = R.color.link
                    etherscan.setOnClickListener {_ ->
                        val webpage = Uri.parse(Etherscan.tx(it.network, hash))
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }

                if (it.status == "completed") {
                    status.setText(R.string.confirmed)
                    status.textColorResource = R.color.success
                    progress.visibility = View.INVISIBLE
                } else if (it.status == "error") {
                    alert(getString(R.string.transfer_failed) + it.error).show()
                }
            }
        }
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
}
