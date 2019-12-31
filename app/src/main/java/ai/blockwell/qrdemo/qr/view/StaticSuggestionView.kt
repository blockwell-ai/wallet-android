package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.Argument
import ai.blockwell.qrdemo.api.ArgumentValue
import ai.blockwell.qrdemo.api.SuggestionArgumentValue
import ai.blockwell.qrdemo.api.TxResponse
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.view_voting_argument.view.*
import org.jetbrains.anko.textColorResource

@SuppressLint("ViewConstructor")
class StaticSuggestionView(context: Context, contract: String, final override val arg: Argument, tx: TxResponse, model: VotingModel)
    : SuggestionView(context, tx, model), ArgumentView {

    override val value: ArgumentValue
        get() = SuggestionArgumentValue(suggestion)

    init {
        contractId = contract
        label.text = arg.label

        if (arg.value != null) {
            update(arg.value)
        } else {
            empty()
        }
    }

    override fun update(newValue: ArgumentValue) {
        if (newValue is SuggestionArgumentValue) {
            setSuggestion(newValue.suggestion)
        } else if (newValue.getValue().isNotEmpty()) {
            try {
                loadSuggestion(newValue.getValue().toInt())
            } catch (e: Exception) {
                Log.e("Suggestion", "Exception loading suggestion", e)
                empty()
            }
        }
    }

    override fun empty() {
        tagView.visibility = View.GONE
        suggestion_text.text = context.getString(R.string.select_in_form)
        suggestion_text.textColorResource = R.color.colorHelper
    }

    override fun setOnClickListener(l: OnClickListener?) {
        layout.setOnClickListener(l)
    }
}
