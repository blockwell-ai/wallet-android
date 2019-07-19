package ai.blockwell.qrdemo.view

import android.content.Context
import android.widget.FrameLayout
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import android.annotation.SuppressLint
import kotlinx.android.synthetic.main.view_static_argument.view.*
import org.jetbrains.anko.layoutInflater
import java.math.BigDecimal
import java.text.DecimalFormat

val format = DecimalFormat("#,###.######")

@SuppressLint("ViewConstructor")
class StaticArgumentView(context: Context, val arg: Argument) : FrameLayout(context), ArgumentView {
    override val value: String
        get() = arg.value ?: ""

    init {
        context.layoutInflater.inflate(R.layout.view_static_argument, this, true)
        label.text = arg.label

        val valueText = if (arg.decimals != null) {
            val d = BigDecimal(arg.value)
            format.format(d)
        } else {
            arg.value
        }

        value_view.text = if (arg.symbol != null) {
            "$valueText ${arg.symbol}"
        } else {
            valueText
        }
    }

    override fun validate() = true
}
