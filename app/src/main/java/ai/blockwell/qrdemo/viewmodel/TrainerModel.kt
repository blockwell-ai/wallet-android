package ai.blockwell.qrdemo.viewmodel

import ai.blockwell.qrdemo.api.ApiClient
import ai.blockwell.qrdemo.api.Proxy
import ai.blockwell.qrdemo.api.TrainerChannel
import ai.blockwell.qrdemo.data.DataStore
import androidx.lifecycle.ViewModel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.delay

class TrainerModel(val client: ApiClient, val proxy: Proxy) : ViewModel() {
    val channel = TrainerChannel(client)

    suspend fun getBalance() = ensureContractReady {
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

    private suspend fun <T : Any> ensureContractReady(block: suspend () -> Result<T, Exception>): Result<T, Exception> {
        while (DataStore.trainerToken.isEmpty()) {
            delay(500)
        }
        return block()
    }

    override fun onCleared() {
        channel.cancel()
    }
}
