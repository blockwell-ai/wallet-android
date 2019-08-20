package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.utils.ArgumentValueTypeAdapter
import android.os.Parcelable
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val gson = GsonBuilder().let {
    it.registerTypeAdapter(ArgumentValue::class.java, ArgumentValueTypeAdapter())

    it.create()
}

/**
 * Logic for login and registration
 */
class Tx(val client: ApiClient) {

    suspend fun get(shortcode: String, params: String) = withContext(Dispatchers.Default) {
        val path = if (params.isEmpty()) {
            shortcode
        } else {
            "$shortcode?$params"
        }
        val response = client.get(path, TxResponse.Deserializer)

        response
    }

    suspend fun submit(shortcode: String, params: String, values: List<ArgumentValue>) = withContext(Dispatchers.Default) {
        val path = if (params.isEmpty()) {
            shortcode
        } else {
            "$shortcode?$params"
        }
        val response = client.postWithAuth(path, DataStore.accessToken, TxResponse.Deserializer,
                SubmitRequest(values))

        response
    }
}

@Parcelize
data class TxResponse(
        val transactionId: String?,
        val shortcode: String,
        val contractId: String,
        val method: String,
        val description: String?,
        val creator: String?,
        val arguments: List<Argument>,
        val address: String?,
        val confirmationLink: String?

) : Parcelable {
    object Deserializer : ResponseDeserializable<TxResponse> {
        override fun deserialize(content: String) = gson.fromJson(content, TxResponse::class.java)
    }
}

@Parcelize
data class Argument(
        val label: String,
        val type: String,
        val dynamic: String?,
        val decimals: Int?,
        val symbol: String?,
        val value: @RawValue ArgumentValue?,
        val help: String?
) : Parcelable


abstract class ArgumentValue {
    open fun isArray() = false
    open fun getValue() = ""
    open fun getArray() = listOf<String>()
}

@Parcelize
data class StringArgumentValue(private val value: String) : ArgumentValue(), Parcelable {
    override fun getValue() = value
}

@Parcelize
data class ArrayArgumentValue(private val values: List<String>) : ArgumentValue(), Parcelable {
    override fun isArray() = true

    override fun getArray(): List<String> {
        return values
    }
}

data class SubmitRequest(
        val arguments: List<ArgumentValue>
)