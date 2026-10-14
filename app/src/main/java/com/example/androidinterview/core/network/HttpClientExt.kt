package com.example.androidinterview.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Performs a GET on [path] (relative to the base URL) and decodes the body as [T].
 *
 * Safe to call from the main thread. The request is built on [Dispatchers.IO] because resolving
 * the base URL (`MockServerManager.baseUrl`) does a host-name lookup, which Android forbids on
 * the main thread (`NetworkOnMainThreadException`).
 *
 * @param path Path relative to the base URL.
 * @param block Extra request configuration, e.g. query parameters.
 * @throws java.io.IOException on network failure
 * @throws HttpException on a non-2xx response
 * @throws kotlinx.serialization.SerializationException on an unexpected payload
 */
suspend inline fun <reified T> HttpClient.getJson(
    path: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {},
): T {
    val body = withContext(Dispatchers.IO) { get(path) { block() }.successBodyOrThrow() }
    return NetworkJson.decodeFromString<T>(body)
}

/**
 * Performs a POST of [body] as JSON on [path] (relative to the base URL) and decodes the answer as [T].
 *
 * Same threading and failure behaviour as [getJson]. Null properties of [body] are not sent.
 *
 * @param path Path relative to the base URL.
 * @param body Request payload, serialized with [NetworkJson].
 * @throws java.io.IOException on network failure
 * @throws HttpException on a non-2xx response
 * @throws kotlinx.serialization.SerializationException on an unexpected payload
 */
suspend inline fun <reified B, reified T> HttpClient.postJson(path: String, body: B): T {
    val payload = NetworkJson.encodeToString(body)
    val response = withContext(Dispatchers.IO) {
        post(path) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.successBodyOrThrow()
    }
    return NetworkJson.decodeFromString<T>(response)
}

/**
 * Reads the response body, failing with [HttpException] when the status is not 2xx.
 *
 * @throws HttpException on a non-2xx response, carrying the parsed API error body if there is one
 */
suspend fun HttpResponse.successBodyOrThrow(): String {
    val body = bodyAsText()
    if (!status.isSuccess()) {
        val error = runCatching { NetworkJson.decodeFromString<ApiErrorDto>(body) }.getOrNull()
        throw HttpException(status.value, error)
    }
    return body
}
