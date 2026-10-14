package com.example.androidinterview.device.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEVICE_ID_KEY = "device_id"

/**
 * [DeviceIdLocalDataSource] backed by [SharedPreferences]; disk access runs on [Dispatchers.IO].
 *
 * @param preferences Preferences file holding the identifier.
 */
class SharedPreferencesDeviceIdLocalDataSource(
    private val preferences: SharedPreferences,
) : DeviceIdLocalDataSource {

    override suspend fun get(): String? = withContext(Dispatchers.IO) {
        preferences.getString(DEVICE_ID_KEY, null)
    }

    override suspend fun save(id: String) = withContext(Dispatchers.IO) {
        preferences.edit(commit = true) { putString(DEVICE_ID_KEY, id) }
    }
}
