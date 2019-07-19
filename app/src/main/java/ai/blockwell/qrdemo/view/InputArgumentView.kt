package ai.blockwell.qrdemo.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.utils.isValidAddress
import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_input_argument.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater

@SuppressLint("ViewConstructor")
class InputArgumentView(context: Context, val arg: Argument) : FrameLayout(context), ArgumentView {

    override val value: String
        get() = input.text.toString()

    init {
        context.layoutInflater.inflate(R.layout.view_input_argument, this, true)

        input_layout.hint = arg.label
        input_layout.helperText = arg.help

        when (arg.type) {
            "address" -> {
                input.textSize = 13f
            }
            "uint" -> {
                if (arg.decimals ?: 0 > 0) {
                    input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                } else {
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                }
            }
        }

        arg.symbol?.let {
            unit.visibility = View.VISIBLE
            unit.text = it
            unit.measure(0, 0)
            input.setPadding(
                    input.paddingLeft,
                    input.paddingTop,
                    unit.measuredWidth + dip(10),
                    input.paddingBottom)
        }
    }

    override fun validate(): Boolean {
        return when (arg.type) {
            "address" -> {
                if (value.isValidAddress()) {
                    input_layout.error = null
                    true
                } else {
                    input_layout.error = "Enter a valid address"
                    false
                }
            }
            "uint" -> {
                if (value.isNotEmpty()) {
                    input_layout.error = null
                    true
                } else {
                    input_layout.error = "Enter a value"
                    false
                }
            }
            else -> true
        }
    }
}
