package com.example.androidinterview.ui.security

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.androidinterview.ui.findActivity

/**
 * Marks the window as secure while this composable is shown: screenshots and screen recordings come
 * out black and the app preview is hidden in Recents. Dialogs inherit it.
 */
@Composable
fun SecureWindowEffect() {
    val window = LocalContext.current.findActivity().window
    DisposableEffect(window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}
