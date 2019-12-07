package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.data.ShortcutScreenConfig
import android.content.Context
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_shortcut_screen.view.*
import org.jetbrains.anko.layoutInflater

class ShortcutScreenView(context: Context) : FrameLayout(context) {

    var config: ShortcutScreenConfig? = null

    init {
        context.layoutInflater.inflate(R.layout.view_shortcut_screen, this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        wrap.setOnClickListener(l)
    }

    fun update(newConfig: ShortcutScreenConfig) {
        config = newConfig
        title.text = newConfig.title
        description.text = newConfig.description
    }
}
