package ai.blockwell.qrdemo.trainer

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

typealias FragmentProvider = () -> Fragment

class GuidedStepsPagerAdapter(val items: List<FragmentProvider>, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val map = mutableMapOf<Int, Fragment>()

    override fun getItem(position: Int): Fragment {
        val frag = items[position]()

        map[position] = frag

        return frag
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        super.destroyItem(container, position, obj)
        map.remove(position)
    }

    override fun getCount() = items.size

    fun getFragment(position: Int) = map[position]
}