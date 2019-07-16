package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_suggestions_step1.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step1Fragment : StepFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the bottom back/next buttons for this screen
        buttons.hideBack()
        buttons.onNextClick {
            model.events.publish(Events.Type.NEXT)
        }

        load()
    }

    fun load() {
        scope.launch {
            // Load the suggestions from the blockchain
            val suggestions = model.getSuggestions()

            if (isAdded) {
                suggestions.fold({
                    // Success, set the suggestions in the list
                    suggestions_list.setSuggestions(it)
                }, {
                    // Failure, show an error
                    requireActivity().alert(R.string.unknown_error).show()
                })
            }
        }
    }
}
