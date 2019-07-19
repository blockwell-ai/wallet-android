package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.data.DataStore
import android.os.Parcelable
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Get the user's Trainer Token
 */
class Trainer(val client: ApiClient) {

    suspend fun get() = withContext(Dispatchers.Default) {
        val response = client.getWithAuth("api/users/me/trainer", DataStore.accessToken, TrainerResponse.Deserializer)

        response
    }
}

@Parcelize
data class TrainerResponse(
        val contractId: String?,
        val address: String?
) : Parcelable {
    object Deserializer : ResponseDeserializable<TrainerResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, TrainerResponse::class.java)
    }
}
