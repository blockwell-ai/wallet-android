package ai.blockwell.qrdemo.trainer

import android.content.Context
import android.widget.FrameLayout
import ai.blockwell.qrdemo.R
import android.annotation.TargetApi
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor
import kotlinx.android.synthetic.main.view_back_next.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.textResource

class BackNextView : FrameLayout {
    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        context.layoutInflater.inflate(R.layout.view_back_next, this, true)
    }

    fun hideNext() {
        next.visibility = View.INVISIBLE
    }
    fun hideBack() {
        back.visibility = View.INVISIBLE
    }
    fun useSkip() {
        next_text.textResource = R.string.skip
        next_text.textColorResource = R.color.colorHelper
        next_icon.imageTintList = ColorStateList.valueOf(getColor(context, R.color.colorHelper))
    }
    fun disableNext() {
        next.isEnabled = false
    }
    fun enableNext() {
        next.isEnabled = true
    }

    fun onNextClick(block: () -> Unit) {
        next.setOnClickListener { block() }
    }

    fun onBackClick(block: () -> Unit) {
        back.setOnClickListener { block() }
    }
}
