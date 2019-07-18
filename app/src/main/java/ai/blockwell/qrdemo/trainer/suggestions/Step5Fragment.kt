package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import kotlinx.android.synthetic.main.fragment_suggestions_step5.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step5Fragment : StepFragment(), Events.Subscriber {
    override val layoutRes = R.layout.fragment_suggestions_step5

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
