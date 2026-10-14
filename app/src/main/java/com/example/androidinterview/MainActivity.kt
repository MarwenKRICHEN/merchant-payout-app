package com.example.androidinterview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidinterview.navigation.AppNavHost
import com.example.androidinterview.ui.theme.AndroidInterviewTheme
import dagger.hilt.android.AndroidEntryPoint

/** Single activity hosting the Compose UI. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidInterviewTheme {
                AppNavHost()
            }
        }
    }
}
