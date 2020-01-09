package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_json_field.view.*
import org.jetbrains.anko.append
import org.jetbrains.anko.layoutInflater

@SuppressLint("ViewConstructor")
class JsonFieldView(context: Context, override val arg: Argument) : FrameLayout(context), ArgumentView {
    override val value: ArgumentValue
        get() = currentValue

    private var currentValue: ArgumentValue = StringArgumentValue("")

    init {
        context.layoutInflater.inflate(R.layout.view_json_field, this, true)
        arg.value?.let { currentValue = it }
        update(currentValue)
    }

    override fun update(newValue: ArgumentValue) {
        currentValue = newValue
        val span = SpannableStringBuilder()
        span.append(arg.getLabel(), StyleSpan(Typeface.BOLD))
        span.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorTextEmphasis)),
                0,
                arg.getLabel().length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.append(" ")

        if (newValue.getValue().isEmpty()) {
            span.append(
                    context.getString(R.string.enter_value_in_form),
                    StyleSpan(Typeface.ITALIC)
            )
            span.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorHelper)),
                    arg.getLabel().length,
                    span.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            span.append(
                    newValue.getValue()
            )
        }

        textView.text = span
    }

    override fun validate() = true
}
