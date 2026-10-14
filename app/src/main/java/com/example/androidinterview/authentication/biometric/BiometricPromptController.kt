package com.example.androidinterview.authentication.biometric

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.androidinterview.authentication.AuthenticationBridge
import com.example.androidinterview.authentication.AuthenticationResult
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Shows the system biometric prompt for the requests published on an [AuthenticationBridge].
 *
 * Create it in `onCreate`, before the Activity is started: the [BiometricPrompt] it owns is then
 * re-attached by the library to the prompt that is still showing after a configuration change, so
 * the prompt is not torn down and shown again. A request already being prompted for is therefore not
 * prompted again by the recreated Activity.
 *
 * @param activity Activity the prompt belongs to.
 * @param bridge Source of the authentication requests, answered with the prompt's outcome.
 * @param title Prompt title.
 * @param subtitle Prompt subtitle.
 * @param negativeText Label of the cancel button.
 */
class BiometricPromptController(
    activity: FragmentActivity,
    private val bridge: AuthenticationBridge,
    title: String,
    subtitle: String,
    negativeText: String,
) {

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            bridge.pending.value?.complete(AuthenticationResult.Succeeded)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            val request = bridge.pending.value ?: return
            if (errorCode == BiometricPrompt.ERROR_CANCELED) {
                // The system tore the prompt down; it is shown again when the Activity is started.
                request.isPromptShown = false
            } else {
                request.complete(biometricErrorToResult(errorCode))
            }
        }
    }

    private val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)

    private val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeText)
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .build()

    init {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bridge.pending.collect { request ->
                    when {
                        request == null -> prompt.cancelAuthentication()
                        !request.isPromptShown -> {
                            request.isPromptShown = true
                            prompt.authenticate(info)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Maps a [BiometricPrompt] error code to the outcome the app cares about.
 *
 * @param errorCode One of the `BiometricPrompt.ERROR_*` constants, except `ERROR_CANCELED`, which
 *   is the system tearing the prompt down and not an outcome.
 */
internal fun biometricErrorToResult(errorCode: Int): AuthenticationResult = when (errorCode) {
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    -> AuthenticationResult.Cancelled
    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
    -> AuthenticationResult.LockedOut
    BiometricPrompt.ERROR_NO_BIOMETRICS -> AuthenticationResult.NotEnrolled
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    -> AuthenticationResult.Unavailable
    else -> AuthenticationResult.Failed
}
