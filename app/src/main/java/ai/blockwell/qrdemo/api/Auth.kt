package ai.blockwell.qrdemo.api

import ai.blockwell.qrdemo.BuildConfig
import ai.blockwell.qrdemo.data.DataStore
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.success
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Logic for login and registration
 */
class Auth(val client: ApiClient) {

    fun isLoggedIn(): Boolean {
        return DataStore.accessToken.isNotEmpty()
                && DataStore.tokenExpiration > 0L
                && DataStore.tokenExpiration > System.currentTimeMillis()
    }

    fun getEmail(): String {
        return DataStore.email
    }

    fun signOut() {
        DataStore.clear()
    }

    suspend fun register(email: String, password: String) = withContext(Dispatchers.Default) {
        val response = client.post("api/auth/register",
                AuthResponse.Deserializer,
                AuthRequest(email, password, DataStore.tokenId))

        response.success { result ->
            DataStore.email = email
            DataStore.accessToken = result.token
            DataStore.tokenExpiration = result.expiration
        }

        response
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.Default) {
        val response = client.post("api/auth/login",
                AuthResponse.Deserializer,
                AuthRequest(email, password, DataStore.tokenId))

        response.success {result ->
            DataStore.email = email
            DataStore.accessToken = result.token
            DataStore.tokenExpiration = result.expiration
        }

        response
    }
}

data class AuthRequest(val email: String, val password: String, val contractId: String)

data class AuthResponse(
        val token: String = "",
        val expiration: Long = 0L,
        val customToken: String? = null
) {
    object Deserializer : ResponseDeserializable<AuthResponse> {
        override fun deserialize(content: String) = Gson().fromJson(content, AuthResponse::class.java)
    }
}
