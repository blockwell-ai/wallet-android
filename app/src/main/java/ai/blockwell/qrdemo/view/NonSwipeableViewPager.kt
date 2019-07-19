package ai.blockwell.qrdemo.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    var swipingEnabled = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipingEnabled) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipingEnabled) {
            super.onTouchEvent(ev)
        } else {
            false
        }
    }
}