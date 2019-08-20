package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.WebViewActivity
import ai.blockwell.qrdemo.api.ApiClient
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ai.blockwell.qrdemo.api.TxResponse
import ai.blockwell.qrdemo.qr.view.ArgumentView
import ai.blockwell.qrdemo.qr.view.InputArgumentView
import ai.blockwell.qrdemo.qr.view.StaticArgumentView
import ai.blockwell.qrdemo.qr.view.VotingArgumentView
import ai.blockwell.qrdemo.viewmodel.TxModel
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_tx.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textResource
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject

/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxActivity : AppCompatActivity() {
    val scope = MainScope()
    val model by viewModel<TxModel>()
    val votingModel by viewModel<VotingModel>()
    lateinit var url: Uri

    var arguments: List<ArgumentView> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tx)
        title = "Transaction"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        url = Uri.parse(intent.getStringExtra("url"))

        accept.setOnClickListener { submit() }
        cancel.setOnClickListener { finish() }

        load()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun load() {
        scope.launch {
            val result = model.getCode(url).await()
            static_arguments.removeAllViews()
            input_arguments.removeAllViews()

            result.fold({
                render(it)
            }, {
                val message = it.message
                if (message != null) {
                    alert(message).show()
                } else {
                    alert(R.string.unknown_error).show()
                }
            })
        }
    }

    private fun render(tx: TxResponse) {
        if (tx.creator != null) {
            val spannable = SpannableStringBuilder(getString(R.string.requesting_tx, tx.creator))
            val bold = StyleSpan(Typeface.BOLD)
            spannable.setSpan(bold, 0, tx.creator.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            creator.text = spannable
        } else {
            creator.textResource = R.string.requesting_tx_no_creator
        }
        if (tx.description != null) {
            description.text = tx.description
        } else {
            description.visibility = View.GONE
        }
        function.text = tx.method
        contract.text = tx.address

        val voting = tx.method == "vote" && tx.arguments.size == 2

        arguments = tx.arguments.mapIndexed { index, it ->
            if (voting && index == 0) {
                VotingArgumentView(this, it, tx, votingModel)
            } else if (it.value != null) {
                StaticArgumentView(this, it) as ArgumentView
            } else {
                InputArgumentView(this, it) as ArgumentView
            }
        }

        arguments.forEach {
            if (it.static) {
                static_arguments.addView(it as ViewGroup)
            } else {
                please_fill.visibility = View.VISIBLE
                input_arguments.addView(it as ViewGroup)
            }
        }
    }

    fun submit() {
        scope.launch {
            if (arguments.find { !it.validate() } == null) {
                accept.isEnabled = false
                cancel.isEnabled = false
                progress.visibility = View.VISIBLE
                accept.text = ""

                val values = arguments.map { it.value }

                val result = model.submitCode(url, values).await()

                result.fold({
                    val link = it.confirmationLink
                    if (!link.isNullOrEmpty()) {
                        startActivity<WebViewActivity>("title" to it.creator, "url" to link)
                    } else {
                        startActivity<TxSuccessActivity>("tx" to it)
                    }
                    finish()
                }, {
                    accept.isEnabled = true
                    cancel.isEnabled = true
                    accept.setText(R.string.accept)
                    progress.visibility = View.GONE
                    longToast(R.string.submit_failed)
                })
            }
        }
    }
}
