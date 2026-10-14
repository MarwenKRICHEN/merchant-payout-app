package com.example.androidinterview

import android.app.Application
import com.example.androidinterview.mock.MockServerManager
import dagger.hilt.android.HiltAndroidApp
import kotlin.concurrent.thread

/** Application entry point: starts the mock API server and the Hilt component. */
@HiltAndroidApp
class InterviewApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        thread(name = "mock-server-init") { MockServerManager.start() }
    }
}
