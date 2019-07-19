package ai.blockwell.qrdemo.generated.base

import ai.blockwell.qrdemo.R
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_generated_option.view.*
import org.jetbrains.anko.layoutInflater

class GeneratedOptionView(context: Context) : FrameLayout(context) {

    var option: String = ""

    init {
        context.layoutInflater.inflate(R.layout.view_generated_option, this, true)
        layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    override fun setOnClickListener(l: OnClickListener?) {
        wrap.setOnClickListener(l)
    }

    fun update(newOption: String) {
        option = newOption
        title.text = option
    }
}
