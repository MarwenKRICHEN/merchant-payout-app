package com.example.androidinterview.core.network

import com.example.androidinterview.mock.MockServerManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest

/**
 * Creates the app's [HttpClient].
 *
 * [baseUrl] is evaluated on every request: the mock server is started on a background
 * thread at app launch, so its URL must not be captured when the client is built.
 */
fun createHttpClient(
    engine: HttpClientEngine = OkHttp.create(),
    baseUrl: () -> String = { MockServerManager.baseUrl },
): HttpClient = HttpClient(engine) {
    expectSuccess = false
    defaultRequest { url(baseUrl()) }
}
