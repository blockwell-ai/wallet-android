package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.Result
import kotlinx.coroutines.async

class VotingModel(val client: ApiClient, val proxy: Proxy) : ViewModel() {
    suspend fun getSuggestion(contractId: String, suggestionId: Int) = viewModelScope.async {
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
}
