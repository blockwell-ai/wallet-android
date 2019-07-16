package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TransactionStatusResponse
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import ai.blockwell.qrdemo.utils.hideKeyboard
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_suggestions_step2.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step2Fragment : StepFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the bottom back/next buttons for this screen
        buttons.hideNext()
        buttons.onBackClick {
            model.events.publish(Events.Type.BACK)
        }

        create.setOnClickListener { create() }
    }

    // This is triggered when the user taps create
    fun create() {
        val text = name.text.toString()

        if (text.isEmpty()) {
            name.error = "Please enter a name"
            return
        }

        // Disable the create button when submitting
        create.isEnabled = false
        hideKeyboard()
        scope.launch {
            // This calls the function in the smart contract to create a suggestion
            val result = model.createSuggestion(text)

            result.fold({
                // Creating the transaction was successful, so now we need to watch for result
                watchTransaction(create, it.id,
                        "Creating suggestion...",
                        "Suggestion created.") {
                    onResult(it)
                }
            }, {
                // Failed, show an error message
                requireActivity().alert(R.string.unknown_error).show()
                create.isEnabled = true
            })
        }
    }

    fun onResult(result: TransactionStatusResponse) {
        scope.launch {
            if (result.status == "completed") {
                // The contract function was successful, these publish events will move the
                // flow forward
                model.events.publish(Events.Type.REFRESH)
                model.events.publish(Events.Type.NEXT)
            } else {
                // The transaction failed, show an error message
                requireActivity().alert(R.string.contract_send_failed).show()
            }
        }
    }
}
