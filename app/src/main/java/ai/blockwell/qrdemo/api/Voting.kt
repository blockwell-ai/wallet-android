package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.trainer.suggestions.Suggestion
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.map
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Get the user's Trainer Token
 */
class Voting(val client: ApiClient) {
    suspend fun getAll(contractId: String) = withContext(Dispatchers.Default) {
        val response = client.get("api/voting/$contractId", SuggestionsResponse.Deserializer)

        response.map { it.suggestions }
    }
    suspend fun getSuggestions(contractId: String) = withContext(Dispatchers.Default) {
        val response = client.get("api/voting/$contractId/suggestions", SuggestionsResponse.Deserializer)

        response.map { it.suggestions }
    }
    suspend fun getProposals(contractId: String) = withContext(Dispatchers.Default) {
        val response = client.get("api/voting/$contractId/proposals", SuggestionsResponse.Deserializer)

        response.map { it.suggestions }
    }
}

@Parcelize
data class SuggestionsResponse(
        val suggestions: List<Suggestion>
) : Parcelable {
    object Deserializer : ResponseDeserializable<SuggestionsResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, SuggestionsResponse::class.java)
    }
}
