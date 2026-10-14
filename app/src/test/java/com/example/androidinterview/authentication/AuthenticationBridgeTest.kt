package com.example.androidinterview.authentication

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthenticationBridgeTest {

    private val bridge = AuthenticationBridge()

    @Test
    fun `request publishes a pending request and returns the outcome it is completed with`() = runTest {
        var result: AuthenticationResult? = null
        launch { result = bridge.request() }
        runCurrent()

        assertNotNull(bridge.pending.value)
        bridge.pending.value!!.complete(AuthenticationResult.Cancelled)
        runCurrent()

        assertEquals(AuthenticationResult.Cancelled, result)
        assertNull(bridge.pending.value)
    }

    @Test
    fun `cancelling the caller clears the pending request`() = runTest {
        val job = launch { bridge.request() }
        runCurrent()
        assertNotNull(bridge.pending.value)

        job.cancel()
        runCurrent()

        assertNull(bridge.pending.value)
    }

    @Test
    fun `a second request waits for the first one`() = runTest {
        val results = mutableListOf<AuthenticationResult>()
        launch { results += bridge.request() }
        launch { results += bridge.request() }
        runCurrent()

        val first = bridge.pending.value!!
        first.complete(AuthenticationResult.Succeeded)
        runCurrent()
        assertEquals(listOf<AuthenticationResult>(AuthenticationResult.Succeeded), results)

        val second = bridge.pending.value!!
        second.complete(AuthenticationResult.Failed)
        runCurrent()
        assertEquals(
            listOf(AuthenticationResult.Succeeded, AuthenticationResult.Failed),
            results,
        )
    }

    @Test
    fun `a new request has not been prompted for yet`() = runTest {
        backgroundScope.launch { bridge.request() }
        runCurrent()

        assertFalse(bridge.pending.value!!.isPromptShown)
    }
}
