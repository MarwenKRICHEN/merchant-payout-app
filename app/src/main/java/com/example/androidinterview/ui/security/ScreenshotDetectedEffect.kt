package com.example.androidinterview.ui.security

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.example.androidinterview.ui.findActivity

/**
 * Calls [onScreenshot] when the user takes a screenshot while this composable is shown and the
 * Activity is started. Needs the `DETECT_SCREEN_CAPTURE` permission.
 *
 * @param onScreenshot Called on the main thread after a screenshot.
 */
@Composable
fun ScreenshotDetectedEffect(onScreenshot: () -> Unit) {
    val activity = LocalContext.current.findActivity()
    val currentOnScreenshot by rememberUpdatedState(onScreenshot)
    LifecycleStartEffect(activity) {
        val callback = Activity.ScreenCaptureCallback { currentOnScreenshot() }
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
        onStopOrDispose { activity.unregisterScreenCaptureCallback(callback) }
    }
}
