package ai.blockwell.qrdemo.api

import android.util.Log
import ai.blockwell.qrdemo.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlin.coroutines.CoroutineContext

/**
 * Get the status of the user's Trainer Token, and updates until it's ready
 */
class TrainerChannel(val client: ApiClient) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private val trainer = Trainer(client)


    val channel: ConflatedBroadcastChannel<TrainerResponse> = ConflatedBroadcastChannel()

    /**
     * Job reference lets us cancel the refresh loop.
     */
    val job: Job

    init {
        job = launch {
            // This blocks the coroutine indefinitely until it's broken
            refreshLoop()
        }
    }

    private suspend fun refreshLoop() {
        while (true) {
            // Stop the loop if the channel is closed permanently
            if (channel.isClosedForSend) {
                break
            }
            if (refreshStatus()) {
                break
            }
            delay(5000)
        }
    }

    /**
     * Refreshes the status of a transaction from the backend.
     */
    private suspend fun refreshStatus(): Boolean {
        if (channel.isClosedForSend) {
            Log.d("TrainerChannel", "Closed for send, cancelling refresh")
            return true
        }

        val result = trainer.get()

        val response = result.component1()

        if (response != null) {
            if (response.contractId != null) {
                DataStore.trainerToken = response.contractId
            }

            if (response != channel.valueOrNull) {
                channel.offer(response)
            }

            if (response.address != null) {
                DataStore.trainerTokenAddress = response.address
                return true
            }
        }

        return false
    }

    /**
     * Cancels both the channel and any pending requests.
     */
    fun cancel() {
        channel.cancel()
        job.cancel()
    }
}