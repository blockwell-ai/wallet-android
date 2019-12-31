package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.*
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.trainer.suggestions.SuggestionType
import ai.blockwell.qrdemo.utils.background
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.google.gson.JsonArray
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class VotingModel(val client: ApiClient, val proxy: Proxy) : ViewModel() {
    private val tx = Tx(client)
    private val voting = Voting(client)

    fun getSuggestion(contractId: String, suggestionId: Int) = viewModelScope.async {
        val textAsync = async { proxy.contractCall(contractId, "getSuggestionText", listOf(suggestionId.toString())) }
        val votesAsync = async { proxy.contractCall(contractId, "getVotes", listOf(suggestionId.toString())) }

        val text = textAsync.await()
        val votes = votesAsync.await()

        val result: Result<Suggestion, Exception> = when {
            text.component2() != null -> Result.error(text.component2() as Exception)
            votes.component2() != null -> Result.error(text.component2() as Exception)
            else -> Result.success(Suggestion.parse(suggestionId, text.get().data.asString, votes.get().data.asString))
        }

        result
    }

    suspend fun getSuggestions(contractId: String, type: SuggestionType = SuggestionType.ALL) = background {
        when (type) {
            SuggestionType.ALL -> voting.getAll(contractId)
            SuggestionType.SUGGESTION -> voting.getSuggestions(contractId)
            SuggestionType.PROPOSAL -> voting.getProposals(contractId)
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

    suspend fun getProposalVoteCode(contractId: String) =
            tx.proposalVoteCode(contractId)
}
