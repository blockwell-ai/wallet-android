package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.suggestions.BaseSuggestionsActivity
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
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

        supportActionBar?.setSubtitle(R.string.select_suggestion)

        name = intent.getStringExtra("name") ?: ""
        contractId = intent.getStringExtra("contractId") ?: ""

        if (name.isEmpty() || contractId.isEmpty()) {
            val dialog = alert(R.string.unknown_error)
            dialog.onCancelled { finish() }
            dialog.show()
        } else {
            load()
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