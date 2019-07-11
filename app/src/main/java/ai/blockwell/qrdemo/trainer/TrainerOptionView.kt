package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import android.content.Context
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_trainer_option.view.*
import org.jetbrains.anko.layoutInflater

class TrainerOptionView(context: Context) : FrameLayout(context) {

    var option: TrainerOption? = null

    init {
        context.layoutInflater.inflate(R.layout.view_trainer_option, this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        wrap.setOnClickListener(l)
    }

    fun update(newOption: TrainerOption) {
        option = newOption
        title.text = newOption.title
        description.text = newOption.description
    }
}
