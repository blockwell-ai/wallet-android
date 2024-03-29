package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_suggestions_step3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step3Fragment : StepFragment(), Events.Subscriber {
    override val layoutRes = R.layout.fragment_suggestions_step3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons.hideBack()
        load()
    }

    override fun onEvent(type: Events.Type, data: Any) {
        if (view != null && type == Events.Type.REFRESH) {
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
