package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.StringArgumentValue
import ai.blockwell.qrdemo.api.gson
import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import com.github.ajalt.timberkt.Timber
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.view_json_argument.view.*
import org.jetbrains.anko.layoutInflater

val mapType = object : TypeToken<Map<String, String>>() {}.type

@SuppressLint("ViewConstructor")
class JsonArgumentView(context: Context, override val arg: Argument) : FrameLayout(context), ArgumentView {
    override val value: ArgumentValue
        get() = currentValue

    private var currentValue: ArgumentValue = StringArgumentValue("")

    private val json = arg.source!!.json!!
    val views: List<ArgumentView>

    init {
        context.layoutInflater.inflate(R.layout.view_json_argument, this, true)
        label.text = arg.getLabel()

        val values = if (arg.value != null) {
            try {
                gson.fromJson<Map<String, String>>(arg.value.getValue(), mapType)
            } catch (e: Exception) {
                Timber.d { "Failed to parse JsonArgumentView value: " + arg.value }
                null
            }
        } else {
            null
        }

        views = json.map {
            val view = JsonFieldView(context, it)
            value_wrap.addView(view)

            if (values != null) {
                val value = values[it.name]
                if (value != null) {
                    view.update(StringArgumentValue(value))
                }
            }

            view
        }
    }

    override fun update(newValue: ArgumentValue) {
        // This does nothing
    }

    override fun validate() = true
}
