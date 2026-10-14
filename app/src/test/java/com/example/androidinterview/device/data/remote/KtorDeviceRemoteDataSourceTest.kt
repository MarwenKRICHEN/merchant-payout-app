package com.example.androidinterview.device.data.remote

import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.core.network.createHttpClient
import com.example.androidinterview.device.data.remote.dto.DeviceDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorDeviceRemoteDataSourceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        KtorDeviceRemoteDataSource(
            createHttpClient(
                engine = MockEngine { request ->
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
                baseUrl = { "http://localhost/" },
            ),
        )

    @Test
    fun `getDevice calls GET api devices and decodes the device id`() = runTest {
        val dataSource = dataSource("""{"device_id":"device_1"}""")

        val device = dataSource.getDevice()

        assertEquals(DeviceDto("device_1"), device)
        assertEquals(HttpMethod.Get, requests.single().method)
        assertEquals("/api/devices", requests.single().url.encodedPath)
    }

    @Test
    fun `getDevice fails with the http status on a server error`() = runTest {
        val dataSource = dataSource("{}", HttpStatusCode.InternalServerError)

        val failure = runCatching { dataSource.getDevice() }.exceptionOrNull()

        assertEquals(500, (failure as HttpException).code)
    }
}
