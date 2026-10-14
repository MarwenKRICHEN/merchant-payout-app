package com.example.androidinterview.payout.data.remote

import com.example.androidinterview.core.network.ApiErrorDto
import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.core.network.createHttpClient
import com.example.androidinterview.payout.data.remote.dto.CreatePayoutRequestDto
import com.example.androidinterview.payout.data.remote.dto.PayoutResponseDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorPayoutRemoteDataSourceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        KtorPayoutRemoteDataSource(
            createHttpClient(
                engine = MockEngine { request ->
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                },
                baseUrl = { "http://localhost/" },
            ),
        )

    private val request = CreatePayoutRequestDto(40000, "GBP", "GB29NWBK60161331926819")

    @Test
    fun `createPayout posts the JSON body without a null device id and decodes the answer`() = runTest {
        val dataSource = dataSource(
            """
            {"id":"pay_001","status":"pending","amount":40000,"currency":"GBP",
             "iban":"GB29NWBK60161331926819","created_at":"2026-05-18T10:00:00Z"}
            """.trimIndent(),
        )

        val response = dataSource.createPayout(request)

        assertEquals(
            PayoutResponseDto(
                id = "pay_001",
                status = "pending",
                amount = 40000,
                currency = "GBP",
                iban = "GB29NWBK60161331926819",
                createdAt = "2026-05-18T10:00:00Z",
            ),
            response,
        )
        val sent = requests.single()
        assertEquals(HttpMethod.Post, sent.method)
        assertEquals("/api/payouts", sent.url.encodedPath)
        assertEquals(
            """{"amount":40000,"currency":"GBP","iban":"GB29NWBK60161331926819"}""",
            (sent.body as TextContent).text,
        )
    }

    @Test
    fun `device id is sent when present`() = runTest {
        val dataSource = dataSource(
            """{"id":"p","status":"pending","amount":1,"currency":"GBP","iban":"x","created_at":"2026-05-18T10:00:00Z"}""",
        )

        dataSource.createPayout(request.copy(deviceId = "device_1"))

        assertTrue((requests.single().body as TextContent).text.contains(""""device_id":"device_1""""))
    }

    @Test
    fun `non-2xx answer throws HttpException with the api error`() = runTest {
        val dataSource = dataSource(
            """{"error":"Insufficient funds","code":"INSUFFICIENT_FUNDS"}""",
            HttpStatusCode.BadRequest,
        )

        val exception = runCatching { dataSource.createPayout(request) }.exceptionOrNull()

        assertTrue(exception is HttpException)
        exception as HttpException
        assertEquals(400, exception.code)
        assertEquals(ApiErrorDto("Insufficient funds", "INSUFFICIENT_FUNDS"), exception.error)
    }
}
