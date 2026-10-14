package com.example.androidinterview.merchant.data.remote

import com.example.androidinterview.core.network.ApiErrorDto
import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.core.network.createHttpClient
import com.example.androidinterview.mock.MockDispatcher
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KtorMerchantRemoteDataSourceTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = KtorMerchantRemoteDataSource(
        createHttpClient(
            engine = MockEngine { request ->
                requests += request
                handler(request)
            },
            baseUrl = { "http://localhost/" },
        ),
    )

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `getMerchant calls GET api merchant and decodes the payload`() = runTest {
        val dataSource = dataSource { json(MERCHANT_FIXTURE) }

        val response = dataSource.getMerchant()

        assertEquals(FakeMerchantRemoteDataSource.sampleResponse(), response)
        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/merchant", request.url.encodedPath)
    }

    @Test
    fun `non-2xx response throws HttpException with the parsed error body`() = runTest {
        val dataSource = dataSource {
            json("""{"error":"Service unavailable","code":"SERVICE_UNAVAILABLE"}""", HttpStatusCode.ServiceUnavailable)
        }

        val exception = runCatching { dataSource.getMerchant() }.exceptionOrNull()

        assertTrue(exception is HttpException)
        exception as HttpException
        assertEquals(503, exception.code)
        assertEquals(ApiErrorDto("Service unavailable", "SERVICE_UNAVAILABLE"), exception.error)
    }

    @Test
    fun `malformed payload throws SerializationException`() = runTest {
        val dataSource = dataSource { json("""{"available_balance":"oops"}""") }

        val exception = runCatching { dataSource.getMerchant() }.exceptionOrNull()

        assertTrue(exception is SerializationException)
    }

    /** Runs against the app's own mock server to prove the DTOs match its real payload. */
    @Test
    fun `decodes the payload served by the app mock server`() = runTest {
        val server = MockWebServer().apply {
            dispatcher = MockDispatcher()
            start()
        }
        try {
            val dataSource = KtorMerchantRemoteDataSource(
                createHttpClient(baseUrl = { server.url("/").toString() }),
            )

            val response = dataSource.getMerchant()

            assertEquals(500000, response.availableBalance)
            assertEquals(25000, response.pendingBalance)
            assertEquals(3, response.activity.size)
        } finally {
            server.shutdown()
        }
    }

    private companion object {
        /** Copy of `app/android/fixtures/merchant.json`. */
        val MERCHANT_FIXTURE = """
            {
              "available_balance": 500000,
              "pending_balance": 25000,
              "currency": "GBP",
              "activity": [
                {
                  "id": "act_001",
                  "type": "deposit",
                  "amount": 150000,
                  "currency": "GBP",
                  "date": "2026-05-18T10:23:00.000Z",
                  "description": "Payment from Customer ABC",
                  "status": "completed"
                },
                {
                  "id": "act_002",
                  "type": "payout",
                  "amount": -50000,
                  "currency": "GBP",
                  "date": "2026-05-17T14:30:00.000Z",
                  "description": "Payout to Bank Account ****1234",
                  "status": "completed"
                },
                {
                  "id": "act_003",
                  "type": "deposit",
                  "amount": 230000,
                  "currency": "GBP",
                  "date": "2026-05-16T09:15:00.000Z",
                  "description": "Payment from Customer XYZ",
                  "status": "completed"
                }
              ]
            }
        """.trimIndent()
    }
}
