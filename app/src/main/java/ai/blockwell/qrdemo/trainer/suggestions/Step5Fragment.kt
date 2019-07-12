package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_suggestions_step5.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step5Fragment : StepFragment(), Events.Subscriber  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions_step5, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        model.events.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        model.events.unsubscribe(this)
    }

    override fun onEvent(type: Events.Type, data: Any) {
        if (type == Events.Type.REFRESH) {
            suggestions_list.loading()
            load()
        }
    }

    fun load() {
        scope.launch {
            val suggestions = model.getSuggestions()

            if (isAdded) {
                suggestions.fold({
                    suggestions_list.setSuggestions(it)
                }, {
                    requireActivity().alert(R.string.unknown_error).show()
                })
            }
        }
    }
}
