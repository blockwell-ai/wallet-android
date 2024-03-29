package ai.blockwell.qrdemo.api

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.coroutines.awaitObjectResult
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.nio.charset.Charset

/**
 * Simplified interface to make HTTP calls with JSON responses.
 */
class ApiClient(val gson: Gson) {

    /**
     * Make an unauthenticated POST call.
     */
    suspend fun <T : Any> post(path: String, deserializer: ResponseDeserializable<T>, body: Any? = null): Result<T, Exception> {
        val request = Fuel.post(path)

        request.body(gson.toJson(body))

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an authenticated POST call.
     */
    suspend fun <T : Any> postWithAuth(path: String, authToken: String, deserializer: ResponseDeserializable<T>, body: Any? = null): Result<T, Exception> {
        val request = Fuel.post(path)

        request.header(Pair("Authorization", "Bearer $authToken"))
        request.body(gson.toJson(body))

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an authenticated POST call.
     */
    suspend fun <T : Any> postWithAuth(path: String, params: Parameters, authToken: String, deserializer: ResponseDeserializable<T>, body: Any? = null): Result<T, Exception> {
        val request = Fuel.post(path, params)

        request.header(Pair("Authorization", "Bearer $authToken"))
        request.body(gson.toJson(body))

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an unauthenticated GET call.
     */
    suspend fun <T : Any> get(path: String, deserializer: ResponseDeserializable<T>): Result<T, Exception> {
        val request = Fuel.get(path)

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an unauthenticated GET call.
     */
    suspend fun <T : Any> get(path: String, params: Parameters, deserializer: ResponseDeserializable<T>): Result<T, Exception> {
        val request = Fuel.get(path, params)

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an authenticated GET call.
     */
    suspend fun <T : Any> getWithAuth(path: String, authToken: String, deserializer: ResponseDeserializable<T>): Result<T, Exception> {
        val request = Fuel.get(path)

        request.header(Pair("Authorization", "Bearer $authToken"))

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Make an authenticated GET call.
     */
    suspend fun <T : Any> getWithAuth(path: String, params: Parameters, authToken: String, deserializer: ResponseDeserializable<T>): Result<T, Exception> {
        val request = Fuel.get(path, params)

        request.header(Pair("Authorization", "Bearer $authToken"))

        return convertResult(request.awaitObjectResult(deserializer))
    }

    /**
     * Convert error responses to JsonObject, and logs the error.
     */
    private fun <T : Any> convertResult(result: Result<T, FuelError>): Result<T, Exception> {
        lateinit var res: Result<T, Exception>

        result.fold({ response ->
            res = Result.of(response)
        }, { error ->
            try {
                val json = gson.fromJson(error.response.data.toString(Charset.defaultCharset()), JsonObject::class.java)
                Log.e("ApiClient", "Exception in API call", error)
                res = if (json == null) {
                    Result.error(error)
                } else {
                    Result.error(ApiException(json))
                }
            } catch (e: JsonSyntaxException) {
                Log.e("ApiClient", "Error reading error response", e)
                res = Result.error(e)
            }
        })

        return res
    }
}

/**
 * Exception that wraps an error response from the API.
 */
class ApiException(val obj: JsonObject) : Exception() {
    override val message: String?
        get() = obj.getAsJsonObject("error").get("message").asString
}
