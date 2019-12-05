package ai.blockwell.qrdemo.suggestions

import ai.blockwell.qrdemo.R
import ai.blockwell.qrdemo.ScanQrActivity.Companion.ETH_REGEX
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.qr.TxActivity
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.github.kittinunf.result.success
import kotlinx.android.synthetic.main.activity_suggestions.*
import kotlinx.android.synthetic.main.dialog_contract_address.view.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

class SuggestionsActivity : BaseSuggestionsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contractId = DataStore.suggestionsToken

        if (contractId.isEmpty()) {
            contractId = "b0747377-ea57-4f84-851e-c19cb43f2894"
        }

        fab.show()
        fab.setOnClickListener {
            createSuggestion()
        }

        loadInfo()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_suggestions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_contract -> {
                contractChangeDialog()
                true
            }
            else -> false
        }
    }

    override fun suggestionClick(suggestion: Suggestion) {
        scope.launch {
            val result = votingModel.getVoteCode(contractId)

            result.fold({
                val url = "https://qr.blockwell.ai/${it.shortcode}"
                startActivity<TxActivity>("url" to url, "suggestion" to suggestion)
            }, {
                val message = it.message
                when {
                    systemStatus.error.isNotEmpty() -> alert(systemStatus.error).show()
                    message != null -> alert(message).show()
                    else -> alert(R.string.unknown_error).show()
                }
            })
        }
    }

    private fun createSuggestion() {
        scope.launch {
            val result = votingModel.getCreateSuggestionCode(contractId)

            result.fold({
                val url = "https://qr.blockwell.ai/${it.shortcode}"
                startActivity<TxActivity>("url" to url)
            }, {
                val message = it.message
                when {
                    systemStatus.error.isNotEmpty() -> alert(systemStatus.error).show()
                    message != null -> alert(message).show()
                    else -> alert(R.string.unknown_error).show()
                }
            })
        }
    }

    private fun loadInfo() {
        scope.launch {
            val name = votingModel.getContractName(contractId).await()

            name.success {
                supportActionBar?.subtitle = it.data.asString
            }
        }
    }

    private fun contractChangeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set Contract Address")
        val view = layoutInflater.inflate(R.layout.dialog_contract_address, layout, false)
        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val address = view.contract_address.text.toString()
            if (!ETH_REGEX.matches(address)) {
                alert(R.string.invalid_address).show()
            } else {
                loadContract(dialog, address)
            }
        }
        builder.create().show()
    }

    private fun loadContract(dialog: DialogInterface, address: String) {
        scope.launch {
            val result = votingModel.getContractId(address)

            result.fold({
                contractId = it.id
                DataStore.suggestionsToken = it.id
                load()
                loadInfo()
                dialog.dismiss()
            }, {
                val message = it.message
                when {
                    systemStatus.error.isNotEmpty() -> alert(systemStatus.error).show()
                    message != null -> alert(message).show()
                    else -> alert(R.string.unknown_error).show()
                }
            })
        }
    }
}