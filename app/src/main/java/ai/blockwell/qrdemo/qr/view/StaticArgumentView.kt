package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.widget.FrameLayout
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.view_static_argument.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.lines
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource
import java.math.BigDecimal
import java.text.DecimalFormat

val format = DecimalFormat("#,###.######")

@SuppressLint("ViewConstructor")
class StaticArgumentView(context: Context, override val arg: Argument) : FrameLayout(context), ArgumentView {
    override val value: ArgumentValue
        get() = currentValue

    private var currentValue: ArgumentValue = StringArgumentValue("")

    init {
        context.layoutInflater.inflate(R.layout.view_static_argument, this, true)
        label.text = arg.label
        if (arg.value != null) {
            update(arg.value)
        }
    }

    override fun update(newValue: ArgumentValue) {
        currentValue = newValue
        textView.setTypeface(null, Typeface.NORMAL)
        textView.textColorResource = R.color.colorText
        textView.lines = 1

        if (newValue.isArray()) {
            textView.maxLines = 999
            textView.minLines = 1
            textView.text = newValue.getArray().joinToString("\n") { format(it) }
        } else {
            if (newValue.getValue().isEmpty()) {
                textView.textResource = R.string.enter_value_in_form
                textView.setTypeface(null, Typeface.ITALIC)
                textView.textColorResource = R.color.colorHelper
            } else {
                textView.text = format(newValue.getValue())
                TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            }
        }
    }

    private fun format(value: String): String {
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
