package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.BuildConfig
import ai.blockwell.qrdemo.data.DataStore
import android.net.Uri
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.success
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

/**
 * Proxy to API Miner using the user's auth
 */
class Proxy(val client: ApiClient) {

    suspend fun contractCall(contractId: String, method: String, args: List<String> = listOf()) = withContext(Dispatchers.Default) {

        val query = args.map { "arg=" + Uri.encode(it) }.joinToString("&")

        val response = client.getWithAuth("api/proxy/contracts/$contractId/call/$method?$query", DataStore.accessToken, CallResponse.Deserializer)

        response
    }

    suspend fun contractSend(contractId: String, method: String, args: List<String> = listOf()) = withContext(Dispatchers.Default) {
        val response = client.postWithAuth(
                "api/proxy/contracts/$contractId/send/$method",
                DataStore.accessToken,
                TransactionResponse.Deserializer,
                SendRequest(args))

        response.map {
            it.data
        }
    }

    suspend fun callAddress(network: String, address: String, type: String, method: String, args: List<String> = listOf()) = withContext(Dispatchers.Default) {

        val query = "type=$type" + args.map { "arg=" + Uri.encode(it) }.joinToString("&")

        val response = client.getWithAuth("api/proxy/contracts/direct/$network/$address/call/$method?$query", DataStore.accessToken, CallResponse.Deserializer)

        response
    }
}

data class CallResponse(
        val data: JsonElement
) {
    object Deserializer : ResponseDeserializable<CallResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, CallResponse::class.java)
    }
}

data class SendRequest(
        val arg: List<String>
)


data class TransactionResponse(
        val data: TransactionStatusResponse
) {
    object Deserializer : ResponseDeserializable<TransactionResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TransactionResponse::class.java)
    }
}

