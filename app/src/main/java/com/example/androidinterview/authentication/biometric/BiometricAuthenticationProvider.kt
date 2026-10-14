package com.example.androidinterview.authentication.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.example.androidinterview.authentication.AuthenticationBridge
import com.example.androidinterview.authentication.AuthenticationProvider
import com.example.androidinterview.authentication.AuthenticationResult

/**
 * [AuthenticationProvider] using strong biometrics.
 *
 * Checks the device first, so that "not set up" is reported without showing a prompt, then hands
 * the request to the UI through [bridge].
 *
 * @param context Application context.
 * @param bridge Shows the prompt in the foreground Activity.
 */
class BiometricAuthenticationProvider(
    private val context: Context,
    private val bridge: AuthenticationBridge,
) : AuthenticationProvider {

    override suspend fun authenticate(): AuthenticationResult =
        when (BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> bridge.request()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthenticationResult.NotEnrolled
            else -> AuthenticationResult.Unavailable
        }
}
