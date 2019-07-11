package ai.blockwell.qrdemo.api

import android.os.Parcelable
import ai.blockwell.qrdemo.data.DataStore
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Logic for login and registration
 */
class Tx(val client: ApiClient) {

    suspend fun get(shortcode: String, params: Parameters) = withContext(Dispatchers.Default) {
        val response = client.get(shortcode, params, TxResponse.Deserializer)

        response
    }

    suspend fun submit(shortcode: String, params: Parameters) = withContext(Dispatchers.Default) {
        val response = client.postWithAuth(shortcode, params, DataStore.accessToken, TxResponse.Deserializer, "{}")

        response
    }
}

@Parcelize
data class TxResponse(
        val transactionId: String?,
        val shortcode: String,
        val contractId: String,
        val method: String,
        val creator: String,
        val arguments: List<Argument>,
        val confirmationLink: String?

) : Parcelable {
    object Deserializer : ResponseDeserializable<TxResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TxResponse::class.java)
    }
}

@Parcelize
data class Argument(
        val label: String?,
        val dynamic: String?,
        val decimals: Int?,
        val symbol: String?,
        val value: String?
) : Parcelable
