package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.CreateQrResponse
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.api.Tx
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class VotingModel(val client: ApiClient, val proxy: Proxy) : ViewModel() {
    private val tx = Tx(client)

    fun getSuggestion(contractId: String, suggestionId: Int) = viewModelScope.async {
        val textAsync = async { proxy.contractCall(contractId, "getSuggestionText", listOf(suggestionId.toString())) }
        val votesAsync = async { proxy.contractCall(contractId, "getVotes", listOf(suggestionId.toString())) }

        val text = textAsync.await()
        val votes = votesAsync.await()

        val result: Result<Suggestion, Exception> = when {
            text.component2() != null -> Result.error(text.component2() as Exception)
            votes.component2() != null -> Result.error(text.component2() as Exception)
            else -> Result.success(Suggestion(suggestionId, text.get().data.asString, votes.get().data.asInt))
        }

        result
    }

    fun getSuggestions(contractId: String) = viewModelScope.async {
        try {
            val count = proxy.contractCall(contractId, "suggestionCount").get().data.asInt
            val votesAsync = async { proxy.contractCall(contractId, "getAllVotes") }
            val texts = getAllSuggestionTexts(contractId, count).get()

            val votes = votesAsync.await().get().data.asJsonArray.mapIndexed { index, element ->
                Suggestion(index, texts[index], element.asString.toInt())
            }

            Result.of(votes)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    fun getContractName(contractId: String) = viewModelScope.async {
        // Proxy through to API Miner
        proxy.contractCall(contractId, "name")
    }

    suspend fun getContractId(address: String) = tx.findContractId(address)

    suspend fun getCreateSuggestionCode(contractId: String) =
            tx.createSuggestionCode(contractId)

    suspend fun getVoteCode(contractId: String) =
            tx.voteCode(contractId)

    /**
     * Gets all suggestion texts using the new method if possible, falls back to old method.
     */
    private suspend fun getAllSuggestionTexts(contractId: String, count: Int)
            : Result<List<String>, Exception> {
        val res = proxy.contractCall(contractId, "getAllSuggestionTexts")

        val texts = res.fold({
            it.data.asString.split("|")
        }, {
            (0 until count)
                    .map {
                        proxy.contractCall(contractId, "getSuggestionText", listOf(it.toString()))
                    }
                    .map {
                        it.get().data.asString
                    }
        })

        return Result.of(texts)
    }
}
