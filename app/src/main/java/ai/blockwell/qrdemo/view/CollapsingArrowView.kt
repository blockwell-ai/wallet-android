package ai.blockwell.qrdemo.view

import ai.blockwell.qrdemo.R
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.ViewPager
import android.graphics.drawable.AnimatedVectorDrawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class CollapsingArrowView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, style: Int = 0) : AppCompatImageView(context, attrs, style) {
    private val collapsing: AnimatedVectorDrawable = context.getDrawable(R.drawable.ic_collapsing_animated) as AnimatedVectorDrawable
    private val expanding: AnimatedVectorDrawable = context.getDrawable(R.drawable.ic_expanding_animated) as AnimatedVectorDrawable

    private var expanded: Boolean = false

    init {
        setImageDrawable(context.getDrawable(R.drawable.ic_collapsing))
    }

    fun toggle() {
        val drawable = if (expanded) {
            collapsing
        } else {
            expanding
        }

        setImageDrawable(drawable)
        drawable.start()
        expanded = !expanded
    }
}