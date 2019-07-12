package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_suggestions_step3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step3Fragment : StepFragment(), Events.Subscriber  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons.hideBack()
        load()

        buttons.onNextClick {
            model.events.publish(Events.Type.NEXT)
        }
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
            delay(1000)
            if (isAdded) {
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
}
