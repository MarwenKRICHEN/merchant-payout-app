package com.example.androidinterview.authentication.biometric

import androidx.biometric.BiometricPrompt
import com.example.androidinterview.authentication.AuthenticationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class BiometricErrorMappingTest {

    @Test
    fun `the user dismissing the prompt maps to cancelled`() {
        listOf(
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        ).forEach { assertEquals(AuthenticationResult.Cancelled, biometricErrorToResult(it)) }
    }

    @Test
    fun `lockouts map to locked out`() {
        assertEquals(AuthenticationResult.LockedOut, biometricErrorToResult(BiometricPrompt.ERROR_LOCKOUT))
        assertEquals(
            AuthenticationResult.LockedOut,
            biometricErrorToResult(BiometricPrompt.ERROR_LOCKOUT_PERMANENT),
        )
    }

    @Test
    fun `missing enrolment and hardware are told apart`() {
        assertEquals(AuthenticationResult.NotEnrolled, biometricErrorToResult(BiometricPrompt.ERROR_NO_BIOMETRICS))
        assertEquals(AuthenticationResult.Unavailable, biometricErrorToResult(BiometricPrompt.ERROR_HW_NOT_PRESENT))
        assertEquals(AuthenticationResult.Unavailable, biometricErrorToResult(BiometricPrompt.ERROR_HW_UNAVAILABLE))
    }

    @Test
    fun `anything else is a failure`() {
        assertEquals(AuthenticationResult.Failed, biometricErrorToResult(BiometricPrompt.ERROR_TIMEOUT))
    }
}
