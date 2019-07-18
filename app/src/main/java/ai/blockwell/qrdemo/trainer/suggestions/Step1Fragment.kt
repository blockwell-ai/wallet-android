package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_suggestions_step1.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step1Fragment : StepFragment() {

    override val layoutRes = R.layout.fragment_suggestions_step1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons.hideBack()
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
