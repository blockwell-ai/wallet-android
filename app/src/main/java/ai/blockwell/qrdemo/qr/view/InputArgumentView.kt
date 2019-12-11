package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.utils.isValidAddress
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_input_argument.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater


@SuppressLint("ViewConstructor")
class InputArgumentView(context: Context, override val dynamic: Dynamic, val symbol: String? = null) : FrameLayout(context), DynamicView {

    override val value: ArgumentValue
        get() {
            return when {
                dynamic.type == "array" -> {
                    val list = input.text.toString().lines()
                    ArrayArgumentValue(list.dropLastWhile { it.isEmpty() })
                }
                dynamic.type == "bool" -> BooleanArgumentValue(input.text.toString())
                else -> StringArgumentValue(input.text.toString())
            }
        }

    var qrListener: (() -> Unit)? = null
    var listener: InputListener? = null

    init {
        context.layoutInflater.inflate(R.layout.view_input_argument, this, true)

        input_layout.hint = dynamic.label
        input_layout.helperText = dynamic.help

        when (dynamic.type) {
            "address" -> {
                input.textSize = 13f
                qr.visibility = View.VISIBLE
                input.setPadding(
                        input.paddingLeft,
                        input.paddingTop,
                        qr.measuredWidth,
                        input.paddingBottom)
            }
            "uint" -> {
                input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            "array" -> {
                input.textSize = 13f
                input.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
            }
        }

        symbol?.let {
            unit.visibility = View.VISIBLE
            unit.text = it
            unit.measure(0, 0)
            input.setPadding(
                    input.paddingLeft,
                    input.paddingTop,
                    unit.measuredWidth + dip(10),
                    input.paddingBottom)
        }

        qr.setOnClickListener { qrListener?.invoke() }

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val argumentValue: ArgumentValue = if (dynamic.type == "array") {
                    ArrayArgumentValue(s.toString().split("\n"))
                } else {
                    StringArgumentValue(s.toString())
                }
                listener?.invoke(dynamic, argumentValue)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun setValue(value: String) {
        input.setText(value)
    }

    override fun setInputListener(block: InputListener) {
        listener = block
    }

    override fun validate(): Boolean {
        return when (dynamic.type) {
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
                if (!value.isArray() && value.getValue().isNotEmpty()) {
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
