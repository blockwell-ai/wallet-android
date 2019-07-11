package ai.blockwell.qrdemo.view

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import kotlinx.android.synthetic.main.view_argument.view.*
import org.jetbrains.anko.layoutInflater
import java.math.BigDecimal
import java.text.DecimalFormat

val format = DecimalFormat("#,###.######")

class ArgumentView(context: Context) : FrameLayout(context) {

    init {
        context.layoutInflater.inflate(R.layout.view_argument, this, true)
    }

    fun update(arg: Argument) {
        label.text = arg.label

        val valueText = if (arg.decimals != null) {
            val d = BigDecimal(arg.value)
            format.format(d)
        } else {
            arg.value
        }

        value.text = if (arg.symbol != null) {
            "$valueText ${arg.symbol}"
        } else {
            valueText
        }
    }
}
