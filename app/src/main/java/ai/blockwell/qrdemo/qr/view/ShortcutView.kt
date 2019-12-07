package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TxResponse
import android.content.Context
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_shortcut.view.*
import org.jetbrains.anko.layoutInflater

class ShortcutView(context: Context) : FrameLayout(context) {

    var code: TxResponse? = null

    init {
        context.layoutInflater.inflate(R.layout.view_shortcut, this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        wrap.setOnClickListener(l)
    }

    fun update(newCode: TxResponse) {
        code = newCode
        title.text = newCode.title
        description.text = newCode.description
        shortcode.text = newCode.shortcode
    }
}
