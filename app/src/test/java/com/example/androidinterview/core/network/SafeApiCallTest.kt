package com.example.androidinterview.core.network

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SafeApiCallTest {

    @Test
    fun `wraps the value on success`() = runTest {
        assertEquals(DataResult.Success(42), safeApiCall { 42 })
    }

    @Test
    fun `IOException maps to Network`() = runTest {
        assertEquals(DataResult.Failure(DataError.Network), safeApiCall { throw IOException("offline") })
    }

    @Test
    fun `HttpException maps to Server with status and api code`() = runTest {
        val result = safeApiCall { throw HttpException(400, ApiErrorDto("Insufficient funds", "INSUFFICIENT_FUNDS")) }

        assertEquals(DataResult.Failure(DataError.Server(400, "INSUFFICIENT_FUNDS")), result)
    }

    @Test
    fun `any other exception maps to Unknown`() = runTest {
        assertEquals(DataResult.Failure(DataError.Unknown), safeApiCall { throw SerializationException("bad") })
    }

    @Test
    fun `cancellation is rethrown, not mapped`() = runTest {
        val thrown = runCatching { safeApiCall { throw CancellationException("cancelled") } }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }
}
