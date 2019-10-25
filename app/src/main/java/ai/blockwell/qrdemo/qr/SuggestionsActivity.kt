package ai.blockwell.qrdemo.qr

import ai.blockwell.qrdemo.BaseActivity
import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.viewmodel.VotingModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_suggestions.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.koin.android.architecture.ext.viewModel

class SuggestionsActivity : BaseActivity() {
    companion object {
        const val REQUEST = 1001
    }

    val votingModel by viewModel<VotingModel>()
    var name = ""
    var contractId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggestions)
        setSupportActionBar(toolbar)
        title = "Suggestions"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setSubtitle(R.string.select_suggestion)

        name = intent.getStringExtra("name") ?: ""
        contractId = intent.getStringExtra("contractId") ?: ""

        if (name.isEmpty() || contractId.isEmpty()) {
            val dialog = alert(R.string.unknown_error)
            dialog.onCancelled { finish() }
            dialog.show()
        } else {
            load()
            suggestions_list.setClickListener {
                val result = Intent("ai.blockwell.qrdemo.SUGGESTION_RESULT")
                result.putExtra("name", name)
                result.putExtra("suggestion", it)
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun load() {
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