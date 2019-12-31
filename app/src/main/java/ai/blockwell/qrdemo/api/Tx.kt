package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.data.DataStore
import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import ai.blockwell.qrdemo.utils.ArgumentValueTypeAdapter
import ai.blockwell.qrdemo.utils.TransactionErrorTypeAdapter
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val gson = GsonBuilder().let {
    it.registerTypeAdapter(ArgumentValue::class.java, ArgumentValueTypeAdapter())
    it.registerTypeAdapter(TransactionError::class.java, TransactionErrorTypeAdapter())

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

    suspend fun submit(shortcode: String, params: String, values: Map<String, ArgumentValue>) = withContext(Dispatchers.Default) {
        val path = if (params.isEmpty()) {
            shortcode
        } else {
            "$shortcode?$params"
        }
        val response = client.postWithAuth(path, DataStore.accessToken, TxResponse.Deserializer,
                SubmitRequest(values))

        response
    }

    suspend fun create(request: CreateQrRequest) = withContext(Dispatchers.Default) {
        client.postWithAuth("api/qr/code", DataStore.accessToken, CreateQrResponse.Deserializer, request)
    }

    suspend fun createSuggestionCode(contractId: String) = withContext(Dispatchers.Default) {
        client.getWithAuth("api/qr/suggestions/create/$contractId", DataStore.accessToken, CreateQrResponse.Deserializer)
    }

    suspend fun voteCode(contractId: String, suggestionId: Int? = null) = withContext(Dispatchers.Default) {
        var url = "api/qr/suggestions/vote/$contractId"

        if (suggestionId != null) {
            url += "?suggestion=$suggestionId"
        }

        client.getWithAuth(url, DataStore.accessToken, CreateQrResponse.Deserializer)
    }

    suspend fun proposalVoteCode(contractId: String, suggestionId: Int? = null) = withContext(Dispatchers.Default) {
        var url = "api/qr/suggestions/proposal/$contractId"

        if (suggestionId != null) {
            url += "?suggestion=$suggestionId"
        }

        client.getWithAuth(url, DataStore.accessToken, CreateQrResponse.Deserializer)
    }

    suspend fun findContractId(address: String) = withContext(Dispatchers.Default) {
        client.getWithAuth("api/qr/contract/${address}", DataStore.accessToken, ContractResponse.Deserializer)
    }

    suspend fun getCodes(shortcodes: List<String>) = withContext(Dispatchers.Default) {
        val codes = shortcodes.joinToString(",")
        client.getWithAuth("api/qr/codes?shortcodes=$codes", DataStore.accessToken, CodesResponse.Deserializer)
    }
}

@Parcelize
data class TxResponse(
        val transactionId: String?,
        val shortcode: String,
        val title: String?,
        val description: String?,
        val creator: String?,
        val confirmationLink: String?,
        val confirmationMessage: String?,
        val steps: List<Step>,
        val dynamic: List<Dynamic>
) : Parcelable {
    object Deserializer : ResponseDeserializable<TxResponse> {
        override fun deserialize(content: String) = gson.fromJson(content, TxResponse::class.java)
    }
}

@Parcelize
data class Step(
        val contractId: String,
        val method: String,
        val arguments: List<Argument>,
        val transactionId: String?,
        val address: String? = null
) : Parcelable

@Parcelize
data class Dynamic(
        val label: String,
        val name: String,
        val type: String,
        val help: String?,
        val contractId: String?
) : Parcelable

@Parcelize
data class Argument(
        val label: String,
        val name: String,
        val type: String,
        val decimals: Int? = null,
        val symbol: String? = null,
        val value: @RawValue ArgumentValue? = null,
        val source: Source? = null
) : Parcelable

@Parcelize
data class Source(
        val type: String,
        val name: String,
        val parameter: String? = null
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
data class BooleanArgumentValue(private val value: String) : ArgumentValue(), Parcelable {
    override fun getValue() = value
}

@Parcelize
data class ArrayArgumentValue(private val values: List<String>) : ArgumentValue(), Parcelable {
    override fun isArray() = true

    override fun getArray(): List<String> {
        return values
    }
}

@Parcelize
data class SuggestionArgumentValue(val suggestion: Suggestion) : ArgumentValue(), Parcelable {
    override fun getValue() = suggestion.id.toString()
}

data class SubmitRequest(
        val dynamics: Map<String, ArgumentValue>
)

data class CreateQrRequest(
        val title: String,
        val steps: List<Step> = emptyList(),
        val dynamic: List<Dynamic> = emptyList()
)

@Parcelize
data class CreateQrResponse(
        val shortcode: String,
        val url: String,
        val image: String
) : Parcelable {
    object Deserializer : ResponseDeserializable<CreateQrResponse> {
        override fun deserialize(content: String) = gson.fromJson(content, CreateQrResponse::class.java)
    }
}

@Parcelize
data class ContractResponse(
        val id: String,
        val address: String?
) : Parcelable {
    object Deserializer : ResponseDeserializable<ContractResponse> {
        override fun deserialize(content: String) = gson.fromJson(content, ContractResponse::class.java)
    }
}

data class CodesResponse(
        val data: List<TxResponse>
) {
    object Deserializer : ResponseDeserializable<CodesResponse> {
        override fun deserialize(content: String) = gson.fromJson(content, CodesResponse::class.java)
    }
}
