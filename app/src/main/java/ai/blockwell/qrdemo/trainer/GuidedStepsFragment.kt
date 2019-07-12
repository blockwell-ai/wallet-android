package ai.blockwell.qrdemo.trainer

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.viewmodel.TrainerModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_guided_steps.*
import org.koin.android.architecture.ext.sharedViewModel

abstract class GuidedStepsFragment : Fragment(), Events.Subscriber {
    val model by sharedViewModel<TrainerModel>()

    lateinit var adapter: GuidedStepsPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_guided_steps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GuidedStepsPagerAdapter(fragmentsList(), fragmentManager!!)
        pager.offscreenPageLimit = 1
        pager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        model.events.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        model.events.unsubscribe(this)
    }

    abstract fun fragmentsList(): List<FragmentProvider>

    override fun onEvent(type: Events.Type, data: Any) {
        if (type == Events.Type.NEXT) {
            if (adapter.count > pager.currentItem + 1) {
                pager.setCurrentItem(pager.currentItem + 1, true)
            }
        } else if (type == Events.Type.BACK) {
            if (pager.currentItem > 0) {
                pager.setCurrentItem(pager.currentItem - 1, true)
            }
        }
    }
}