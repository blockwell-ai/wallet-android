package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_suggestions_step4.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step4Fragment : StepFragment(), Events.Subscriber {
    override val layoutRes = R.layout.fragment_suggestions_step4

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons.useSkip()
        load()
    }

    override fun onEvent(type: Events.Type, data: Any) {
        if (type == Events.Type.REFRESH) {
            load()
        }
    }

    fun load() {
        scope.launch {
            val suggestions = model.getSuggestions()

            if (isAdded) {
                suggestions.fold({
                    spinner.setItems(it)
                }, {
                    requireActivity().alert(R.string.unknown_error).show()
                })
            }
        }
    }

    override fun submit() {
        val item = spinner.getItems<Suggestion>()[spinner.selectedIndex]

        if (item is Suggestion) {
            submit.isEnabled = false
            scope.launch {
                val result = model.voteOnSuggestion(item.index)

                result.fold({
                    watchTransaction(it.id, "Voting...", "Voted successfully.") {
                        model.events.publish(Events.Type.NEXT)
                        submit.isEnabled = true
                    }
                }, {
                    requireActivity().alert(R.string.unknown_error).show()
                    submit.isEnabled = true
                })
            }
        }
    }
}
