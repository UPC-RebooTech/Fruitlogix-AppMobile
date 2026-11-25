package com.rebootech.fruitlogix.shared.core.network

import android.util.Log
import com.google.gson.JsonParser
import retrofit2.Response

/** What went wrong in a call, ready for the screen: the server's own text when it sent one. */
data class ApiFailure(
    /** 0 when there was no answer (no signal, timeout). */
    val code: Int,
    /** message, detail, title, first validation error or plain string; null when the body had none. */
    val serverMessage: String?
) {
    val isNetwork get() = code == 0
    val isForbidden get() = code == 403
    val isNotFound get() = code == 404
    val isConflict get() = code == 409
}

/**
 * Reads error bodies in the three shapes the backend uses (CAMBIOS-API.md §2):
 * { "message": … }, a JSON string, or ProblemDetails ({ title, detail } or { errors: { field: [..] } }).
 * 500 answers carry a traceId, which is logged so it can be reported to the backend team.
 */
object ApiErrors {

    private const val TAG = "FruitLogix-API"

    fun from(response: Response<*>): ApiFailure {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        val message = parseMessage(raw)
        if (response.code() >= 500) {
            Log.w(TAG, "HTTP ${response.code()} ${response.raw().request.url.encodedPath} traceId=${parseField(raw, "traceId")}")
        }
        return ApiFailure(response.code(), message)
    }

    fun network(error: Throwable): ApiFailure {
        Log.w(TAG, "No answer: ${error.javaClass.simpleName}")
        return ApiFailure(0, null)
    }

    /** message → detail → plain string → first validation error → title. */
    fun parseMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val element = JsonParser.parseString(raw)
            when {
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    fun text(key: String) = obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
                    text("message") ?: text("detail") ?: obj.getAsJsonObject("errors")?.entrySet()?.firstOrNull()?.value
                        ?.let { if (it.isJsonArray) it.asJsonArray.firstOrNull()?.asString else it.asString }
                        ?: text("title")
                }
                else -> null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun parseField(raw: String?, key: String): String? = runCatching {
        JsonParser.parseString(raw.orEmpty()).asJsonObject.get(key)?.asString
    }.getOrNull()
}

/** Result of a call that may fail with a server message. */
sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>
    data class Failed(val failure: ApiFailure) : ApiResult<Nothing>
}

/** Runs a Retrofit call and turns any outcome into an [ApiResult] (cancellation is rethrown). */
suspend fun <T> apiCall(call: suspend () -> Response<T>): ApiResult<T> {
    val response = try {
        call()
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
        throw e
    } catch (e: Throwable) {
        return ApiResult.Failed(ApiErrors.network(e))
    }
    val body = response.body()
    return when {
        response.isSuccessful && body != null -> ApiResult.Ok(body)
        // 204 No Content: success without a body
        response.isSuccessful -> @Suppress("UNCHECKED_CAST") ApiResult.Ok(Unit as T)
        else -> ApiResult.Failed(ApiErrors.from(response))
    }
}
