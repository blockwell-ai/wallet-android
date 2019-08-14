package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TransactionStatusResponse
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import ai.blockwell.qrdemo.utils.hideKeyboard
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_suggestions_step2.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step2Fragment : StepFragment() {
    override val layoutRes = R.layout.fragment_suggestions_step2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttons.useSkip()
    }

    override fun submit() {
        val text = text.text.toString()

        if (text.isEmpty()) {
            text.error = "Please enter a name"
            return
        }

        // Disable the create button when submitting
        submit.isEnabled = false
        hideKeyboard()
        scope.launch {
            // This calls the function in the smart contract to create a suggestion
            val result = model.createSuggestion(text)

            result.fold({
                // Creating the transaction was successful, so now we need to watch for result
                watchTransaction(it.id,
                        "Creating suggestion...",
                        "Suggestion created.") {
                    onResult(it)
                }
            }, {
                // Failed, show an error message
                requireActivity().alert(R.string.unknown_error).show()
                submit.isEnabled = true
            })
        }
    }

    fun onResult(result: TransactionStatusResponse) {
        scope.launch {
            if (result.status == "completed") {
                // The contract function was successful, these publish events will move the
                // flow forward
                model.events.publish(Events.Type.NEXT)
            } else {
                // The transaction failed, show an error message
                requireActivity().alert(R.string.contract_send_failed).show()
            }
        }
    }
}
