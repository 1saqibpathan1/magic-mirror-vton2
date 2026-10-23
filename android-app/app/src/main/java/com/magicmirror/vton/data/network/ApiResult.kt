package com.magicmirror.vton.data.network

import com.magicmirror.vton.data.network.dto.ApiErrorEnvelope
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val code: String, val message: String) : ApiResult<Nothing>()
}

private val errorJson = Json { ignoreUnknownKeys = true }

/** Wraps a Retrofit suspend call, turning the {"error": {code, message}} envelope
 * from API_CONTRACT.md (and plain network failures) into a typed [ApiResult]. */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        val parsed = body?.let {
            runCatching { errorJson.decodeFromString(ApiErrorEnvelope.serializer(), it) }.getOrNull()
        }
        ApiResult.Failure(
            code = parsed?.error?.code ?: "http_${e.code()}",
            message = parsed?.error?.message ?: (e.message() ?: "Request failed"),
        )
    } catch (e: IOException) {
        ApiResult.Failure("network_error", "Couldn't reach the server. Check the connection.")
    } catch (e: Exception) {
        ApiResult.Failure("unknown_error", e.message ?: "Something went wrong")
    }
}
