package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.suggestions.BaseSuggestionsActivity
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.trainer.suggestions.SuggestionType
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.jetbrains.anko.alert

class QrSuggestionsActivity : BaseSuggestionsActivity() {
    companion object {
        const val REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        name = intent.getStringExtra("name") ?: ""
        contractId = intent.getStringExtra("contractId") ?: ""
        val type = SuggestionType.valueOf(intent.getStringExtra("type") ?: "SUGGESTION")

        if (type == SuggestionType.SUGGESTION) {
            supportActionBar?.setSubtitle(R.string.select_suggestion)
        } else {
            supportActionBar?.setSubtitle(R.string.select_proposal)
            title = getString(R.string.proposals)
        }

        if (name.isEmpty() || contractId.isEmpty()) {
            val dialog = alert(R.string.unknown_error)
            dialog.onCancelled { finish() }
            dialog.show()
        } else {
            load(type)
        }
    }

    override fun suggestionClick(suggestion: Suggestion) {
        val result = Intent("ai.blockwell.qrdemo.SUGGESTION_RESULT")
        result.putExtra("name", name)
        result.putExtra("suggestion", suggestion)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}