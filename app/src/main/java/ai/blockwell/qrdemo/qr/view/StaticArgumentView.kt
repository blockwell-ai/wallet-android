package ai.blockwell.qrdemo.qr.view

import android.content.Context
import android.widget.FrameLayout
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
import android.annotation.SuppressLint
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.view_static_argument.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.lines
import java.math.BigDecimal
import java.text.DecimalFormat

val format = DecimalFormat("#,###.######")

@SuppressLint("ViewConstructor")
class StaticArgumentView(context: Context, val arg: Argument) : FrameLayout(context), ArgumentView {
    override val static = true

    override val value: ArgumentValue
        get() = arg.value ?: StringArgumentValue("")

    init {
        context.layoutInflater.inflate(R.layout.view_static_argument, this, true)
        label.text = arg.label

        if (arg.value != null) {
            if (arg.value.isArray()) {
                arg.value.getArray().forEach {
                    layout.addView(textView(format(it, arg)))
                }
            } else {
                layout.addView(textView(arg.value.getValue()))
            }
        }
    }

    private fun textView(text: String): TextView {
        val view = AppCompatTextView(context)
        view.text = text
        view.textSize = 16f
        view.lines = 1
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(view, 10, 16, 1, COMPLEX_UNIT_SP)
        return view
    }

    private fun format(value: String, arg: Argument): String {
        val valueText = if (arg.decimals != null) {
            val d = BigDecimal(value)
            format.format(d)
        } else {
            value
        }

        return if (arg.symbol != null) {
            "$valueText ${arg.symbol}"
        } else {
            valueText
        }
    }

    override fun validate() = true
}
