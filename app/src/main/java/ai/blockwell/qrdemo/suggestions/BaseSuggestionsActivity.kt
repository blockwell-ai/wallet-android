package ai.blockwell.qrdemo.suggestions

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_suggestions.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.koin.android.architecture.ext.viewModel

abstract class BaseSuggestionsActivity : BaseActivity() {

    val votingModel by viewModel<VotingModel>()
    var name = ""
    var contractId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)
        setSupportActionBar(toolbar)
        title = "Suggestions"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        suggestions_list.setClickListener {
            suggestionClick(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    protected open fun suggestionClick(suggestion: Suggestion) {

    }

    protected fun load() {
        scope.launch {
            val result = votingModel.getSuggestions(contractId).await()
            result.fold({
                suggestions_list.setSuggestions(it)
            }, {
                val message = if (systemStatus.error.isNotEmpty()) {
                    systemStatus.error
                } else {
                    getString(R.string.unknown_error) + " - " + it.message
                }
                val dialog = alert(message)
                dialog.onCancelled { finish() }
                dialog.show()
            })
        }
    }
}