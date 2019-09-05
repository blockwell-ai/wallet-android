package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.view_voting_argument.view.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.textColorResource

@SuppressLint("ViewConstructor")
class InputSuggestionView(context: Context, override val dynamic: Dynamic, tx: TxResponse, model: VotingModel) : SuggestionView(context, tx, model), DynamicView {

    override val value: ArgumentValue
        get() = SuggestionArgumentValue(suggestion)

    var listener: InputListener? = null

    init {
        label.text = dynamic.label
        help.text = dynamic.help
        contractId = dynamic.contractId!!

        layout.isClickable = true
        empty()
    }

    override fun setSuggestion(sugg: Suggestion) {
        super.setSuggestion(sugg)
        listener?.invoke(dynamic, SuggestionArgumentValue(sugg))
    }

    override fun empty() {
        tagView.visibility = View.GONE
        suggestion_text.text = context.getString(R.string.select_suggestion)
        suggestion_text.textColorResource = R.color.colorHelper
    }

    override fun validate(): Boolean {
        return if (suggestion.index == -1) {
            help.textColorResource = R.color.error
            help.text = context.getString(R.string.select_what_to_vote)
            false
        } else {
            help.textColorResource = R.color.colorHelper
            help.text = dynamic.help
            true
        }
    }

    override fun setInputListener(block: InputListener) {
        listener = block
    }

    override fun setOnClickListener(l: OnClickListener?) {
        layout.setOnClickListener(l)
    }
}
