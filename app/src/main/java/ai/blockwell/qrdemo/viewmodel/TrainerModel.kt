package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.api.TrainerChannel
import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.trainer.Events
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class TrainerModel(val client: ApiClient, val proxy: Proxy) : ViewModel() {
    // This is a channel that provides the status of the user's own Trainer Token
    val channel = TrainerChannel(client)
    val events = Events()
    var decimals: Int? = null

    suspend fun getDecimals(): Int {
        val dec = decimals
        return if (dec == null) {
            val value = call("decimals").get().data.asString.toInt()
            decimals = value
            value
        } else {
            dec
        }
    }

    suspend fun getBalance() = ensureContractReady {
        // Proxy through to API Miner
        proxy.contractCall(DataStore.trainerToken, "balanceOf", listOf(DataStore.accountAddress))
    }

    suspend fun getMintingStatus() = ensureContractReady {
        proxy.contractCall(DataStore.trainerToken, "mintEnabled")
    }

    suspend fun enableMinting() = ensureContractReady {
        proxy.contractSend(DataStore.trainerToken, "enableMinting")
    }

    suspend fun mintTokens(recipient: String, value: String) = ensureContractReady {
        proxy.contractSend(DataStore.trainerToken, "mint", listOf(recipient, value))
    }

    suspend fun getSuggestions() = ensureContractReady {
        val count = call("suggestionCount").get().data.asInt

        val votesDeferred = viewModelScope.async { call("getAllVotes") }
        val texts = (0 until count)
                .map {
                    viewModelScope.async { call("getSuggestionText", listOf(it.toString())) }
                }
                .map {
                    it.await().get().data.asString
                }

        val votes = votesDeferred.await().get().data.asJsonArray.mapIndexed { index, element ->
            Suggestion.parse(index, texts[index], element.asString)
        }

        Result.success(votes)
    }

    suspend fun createSuggestion(text: String) = ensureContractReady {
        send("createSuggestion", listOf(text))
    }

    suspend fun voteOnSuggestion(index: Int) = ensureContractReady {
        send("vote", listOf(index.toString(), ""))
    }

    suspend fun call(method: String, args: List<String> = listOf()) = ensureContractReady { proxy.contractCall(DataStore.trainerToken, method, args) }
    suspend fun send(method: String, args: List<String> = listOf()) = ensureContractReady { proxy.contractSend(DataStore.trainerToken, method, args) }

    // A suspending function that makes sure the user's Trainer Token is ready before proceeding
    private suspend fun <T : Any> ensureContractReady(block: suspend () -> Result<T, Exception>): Result<T, Exception> {
        while (DataStore.trainerTokenAddress.isEmpty()) {
            delay(500)
        }
        return try {
            block()
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun onCleared() {
        channel.cancel()
    }
}
