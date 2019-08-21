package ai.blockwell.qrdemo.api

import android.util.Log
import ai.blockwell.qrdemo.data.DataStore
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlin.coroutines.CoroutineContext

/**
 * Listen to the status of a transaction.
 *
 * Note that this needs to have [cancel] called in order for the updates to stop.
 */
class TransactionStatusChannel(val client: ApiClient, val txId: String) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    /**
     * Channel for receiving new transaction statuses.
     *
     * Conflated broadcast channels only send the latest result to new subscribers,
     * and then every update while they're subscribed.
     */
    val channel: BroadcastChannel<TransactionStatusResponse> = ConflatedBroadcastChannel()

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
            refreshStatus()
            delay(5000)
        }
    }

    /**
     * Refreshes the status of a transaction from the backend.
     */
    suspend fun refreshStatus() {
        val response = client.getWithAuth("api/tokens/transactions/$txId",
                DataStore.accessToken,
                TransactionStatusResponse.Deserializer)

        response.success { result ->
            // We only care about a completed or error status
            if (result.status == "completed" || result.status == "error") {
                try {
                    // Clear the pending transaction in the cache
                    if (DataStore.pendingTransfer == txId) {
                        DataStore.pendingTransfer = ""
                    }
                } catch (e: Exception) {
                    Log.e("TransactionStatusChanne", "DataStore exception", e)
                }

                if (!channel.isClosedForSend) {
                    channel.offer(result)
                    // If the channel isn't already canceled, do that now
                    cancel()
                }
            }
        }
    }

    /**
     * Cancels both the channel and any pending requests.
     */
    fun cancel() {
        channel.cancel()
        job.cancel()
    }
}

@Parcelize
data class TransactionStatusResponse(
        val id: String,
        val status: String,
        val from: String,
        val contractId: String?,
        val error: String?,
        val transactionHash: String?,
        val network: String?,
        val events: List<LogEvent?>?
) : Parcelable {
    object Deserializer : ResponseDeserializable<TransactionStatusResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TransactionStatusResponse::class.java)
    }
}

@Parcelize
data class LogEvent(
        val event: String,
        val address: String,
        val returnValues: Map<String, String>
) : Parcelable
