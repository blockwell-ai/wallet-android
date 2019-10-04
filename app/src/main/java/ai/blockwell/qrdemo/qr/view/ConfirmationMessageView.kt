package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TransactionStatusResponse
import ai.blockwell.qrdemo.api.TxResponse
import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.samskivert.mustache.Mustache
import kotlinx.android.synthetic.main.view_confirmation_message.view.*
import org.jetbrains.anko.layoutInflater

@SuppressLint("ViewConstructor")
class ConfirmationMessageView(context: Context) : FrameLayout(context) {

    init {
        context.layoutInflater.inflate(R.layout.view_confirmation_message, this, true)
    }

    fun update(code: TxResponse, tx: TransactionStatusResponse, message: String) {
        val data = HashMap<String, Any>()
        data["code"] = code
        data["tx"] = tx

        val tmpl = Mustache.compiler().compile(message)

        text_view.text = tmpl.execute(data)
    }
}
