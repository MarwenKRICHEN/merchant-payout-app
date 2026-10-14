package com.example.androidinterview

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.androidinterview.authentication.AuthenticationBridge
import com.example.androidinterview.authentication.biometric.BiometricPromptController
import com.example.androidinterview.navigation.AppNavHost
import com.example.androidinterview.ui.theme.AndroidInterviewTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Single activity hosting the Compose UI; a [FragmentActivity] because the biometric prompt needs one. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var authenticationBridge: AuthenticationBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Created here, before the Activity starts, so that the library re-attaches it to a prompt
        // that is still showing after a configuration change.
        BiometricPromptController(
            activity = this,
            bridge = authenticationBridge,
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_subtitle),
            negativeText = getString(R.string.payout_cancel),
        )
        enableEdgeToEdge()
        setContent {
            AndroidInterviewTheme {
                AppNavHost()
            }
        }
    }
}
