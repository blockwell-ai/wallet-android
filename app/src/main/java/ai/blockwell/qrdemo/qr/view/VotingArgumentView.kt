package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_voting_argument.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource

@SuppressLint("ViewConstructor")
class VotingArgumentView(context: Context, val arg: Argument, val tx: TxResponse, val model: VotingModel) : FrameLayout(context), ArgumentView {
    override val static: Boolean
        get() = readOnly

    val scope = MainScope()

    override val value: ArgumentValue
        get() = StringArgumentValue(suggestion.index.toString())

    public var suggestion = Suggestion(-1, "", 0)
        private set

    private var readOnly = false

    init {
        context.layoutInflater.inflate(R.layout.view_voting_argument, this, true)

        label.text = arg.label
        help.text = arg.help

        if (arg.value != null) {
            readOnly = true

            loadSuggestion(arg.value.getValue().toInt())
        } else {
            layout.isClickable = true
        }
    }

    fun setSuggestion(sugg: Suggestion) {
        suggestion = sugg

        render(sugg)
    }

    private fun render(sugg: Suggestion) {
        suggestion_text.text = sugg.text

        if (sugg.tag.isNotEmpty()) {
            tagView.visibility = View.VISIBLE
            tagView.text = sugg.tag
        } else {
            tagView.visibility = View.GONE
        }
    }

    override fun validate(): Boolean {
        return if (suggestion.index == -1) {
            help.textColorResource = R.color.error
            help.text = "Select what you're voting for by tapping here"
            false
        } else {
            true
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        layout.setOnClickListener(l)
    }

    private fun loadSuggestion(suggestionId: Int) {
        scope.launch {
            val result = model.getSuggestion(tx.contractId, suggestionId).await()

            result.fold({
                setSuggestion(it)
            }, {
                val message = it.message
                if (message != null) {
                    context.alert(message).show()
                } else {
                    context.alert(R.string.unknown_error).show()
                }
            })
        }
    }
}
