package ai.blockwell.qrdemo.qr.view

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.api.TxResponse
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
abstract class SuggestionView(context: Context, val tx: TxResponse, val model: VotingModel) : FrameLayout(context) {
    val scope = MainScope()

    var suggestion = Suggestion(-1, "", 0)
        private set

    lateinit var contractId: String

    init {
        context.layoutInflater.inflate(R.layout.view_voting_argument, this, true)
    }

    open fun setSuggestion(sugg: Suggestion) {
        suggestion = sugg

        render(sugg)
        validate()
    }

    protected open fun render(sugg: Suggestion) {
        suggestion_text.text = sugg.text
        suggestion_text.textColorResource = R.color.colorText

        if (sugg.tag.isNotEmpty()) {
            tagView.visibility = View.VISIBLE
            tagView.text = sugg.tag
        } else {
            tagView.visibility = View.GONE
        }
    }

    abstract fun empty()

    open fun validate(): Boolean {
        return true
    }

    override fun setOnClickListener(l: OnClickListener?) {
        layout.setOnClickListener(l)
    }

    protected fun loadSuggestion(suggestionId: Int) {
        scope.launch {
            val result = model.getSuggestion(contractId, suggestionId).await()

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
