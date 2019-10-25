package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.BuildConfig
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
class Tokens(val client: ApiClient) {

    suspend fun get() = withContext(Dispatchers.Default) {
        val response = client.get("api/tokens/${BuildConfig.TOKEN_ID}/properties", TokensResponse.Deserializer)

        response.success { result ->
            DataStore.tokenId = BuildConfig.TOKEN_ID
            DataStore.tokenName = result.name
            DataStore.tokenSymbol = result.symbol
            DataStore.tokenDecimals =  result.decimals.toInt()
        }

        response
    }

}

@Parcelize
data class TokensResponse(
        val name: String,
        val symbol: String,
        val decimals: String

) : Parcelable {
    object Deserializer : ResponseDeserializable<TokensResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TokensResponse::class.java)
    }
}