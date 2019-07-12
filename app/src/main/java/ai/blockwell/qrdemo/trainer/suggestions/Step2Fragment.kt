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

        buttons.hideNext()
        buttons.onBackClick {
            model.events.publish(Events.Type.BACK)
        }

        create.setOnClickListener { create() }
    }

    fun create() {
        val text = name.text.toString()

        if (text.isEmpty()) {
            name.error = "Please enter a name"
            return
        }

        create.isEnabled = false
        hideKeyboard()
        scope.launch {
            val result = model.createSuggestion(text)

            result.fold({
                watchTransaction(create, it.id, "Creating suggestion...", "Suggestion created.") {
                    onResult(it)
                }
            }, {
                requireActivity().alert(R.string.unknown_error).show()
                create.isEnabled = true
            })
        }
    }

    fun onResult(result: TransactionStatusResponse) {
        scope.launch {
            if (result.status == "completed") {
                model.events.publish(Events.Type.REFRESH)
                model.events.publish(Events.Type.NEXT)
            } else {
                requireActivity().alert(R.string.contract_send_failed).show()
            }
        }
    }
}
