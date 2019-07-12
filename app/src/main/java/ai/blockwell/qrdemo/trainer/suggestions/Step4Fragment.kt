package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.StepFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_suggestions_step4.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert

class Step4Fragment : StepFragment(), Events.Subscriber {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_suggestions_step4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttons.hideNext()
        load()

        buttons.onBackClick {
            model.events.publish(Events.Type.BACK)
        }

        vote.setOnClickListener { vote() }
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
            load()
        }
    }

    fun load() {
        scope.launch {
            val suggestions = model.getSuggestions()

            if (isAdded) {
                suggestions.fold({
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
                            .also { adapter ->
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinner.adapter = adapter
                            }
                }, {
                    requireActivity().alert(R.string.unknown_error).show()
                })
            }
        }
    }

    fun vote() {
        val item = spinner.selectedItem

        if (item is Suggestion) {
            vote.isEnabled = false
            scope.launch {
                val result = model.voteOnSuggestion(item.index)

                result.fold({
                    watchTransaction(vote, it.id, "Voting...", "Voted successfully.") {
                        model.events.publish(Events.Type.REFRESH)
                        model.events.publish(Events.Type.NEXT)
                        vote.isEnabled = true
                    }
                }, {
                    requireActivity().alert(R.string.unknown_error).show()
                    vote.isEnabled = true
                })
            }
        }
    }
}
