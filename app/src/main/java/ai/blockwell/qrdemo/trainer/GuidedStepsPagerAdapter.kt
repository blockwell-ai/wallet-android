package ai.blockwell.qrdemo.trainer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

typealias FragmentProvider = () -> Fragment

class GuidedStepsPagerAdapter(val items: List<FragmentProvider>, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int) = items[position]()

    override fun getCount() = items.size
}