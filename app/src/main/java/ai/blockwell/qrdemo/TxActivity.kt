package ai.blockwell.qrdemo

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
import ai.blockwell.qrdemo.view.InputArgumentView
import ai.blockwell.qrdemo.view.StaticArgumentView
import ai.blockwell.qrdemo.viewmodel.TxModel
import kotlinx.android.synthetic.main.activity_tx.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.koin.android.architecture.ext.viewModel

/**
 * Shows a QR code of the user's Ethereum address.
 */
class TxActivity : AppCompatActivity() {
    val scope = MainScope()
    val model by viewModel<TxModel>()
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

            description.text = spannable
        }
        function.text = tx.method
        contract.text = tx.address

        arguments = tx.arguments.map {
            if (it.value != null) {
                StaticArgumentView(this, it) as ArgumentView
            } else {
                InputArgumentView(this, it) as ArgumentView
            }
        }

        arguments.forEach {
            if (it is StaticArgumentView) {
                static_arguments.addView(it)
            } else if (it is InputArgumentView) {
                please_fill.visibility = View.VISIBLE
                input_arguments.addView(it)
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
