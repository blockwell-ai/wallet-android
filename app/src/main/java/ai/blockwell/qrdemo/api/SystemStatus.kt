package ai.blockwell.qrdemo.api

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SystemStatus(val client: ApiClient) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    var status: String = "ok"
        private set
    var message: String = ""
        private set
    var error: String = ""
        private set

    private val subscribers = mutableListOf<StatusSubscriber>()
    private var job: Job? = null

    fun subscribe(subscriber: StatusSubscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber)
            run()
        }
    }

    fun unsubscribe(subscriber: StatusSubscriber) {
        subscribers.remove(subscriber)
    }

    private fun run() {
        val j = job
        if (j == null || j.isCancelled || j.isCompleted) {
            job = launch {
                while (isActive) {
                    val result = client.get("api/status", StatusResponse.Deserializer)

                    result.success {
                        val oldStatus = status
                        status = it.status
                        message = it.message
                        error = it.error

                        if (oldStatus != status) {
                            notifySubscribers()
                        }
                    }

                    delay(10000)

                    if (subscribers.size == 0) {
                        cancel()
                        break
                    }
                }
            }
        }
    }

    private fun notifySubscribers() {
        subscribers.forEach { it.onStatusChanged(status, message) }
    }

    interface StatusSubscriber {
        fun onStatusChanged(status: String, message: String)
    }
}

data class StatusResponse(
        val status: String = "",
        val message: String = "",
        val error: String = ""
) {
    object Deserializer : ResponseDeserializable<StatusResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, StatusResponse::class.java)
    }
}
