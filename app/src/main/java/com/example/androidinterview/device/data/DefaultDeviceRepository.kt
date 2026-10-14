package com.example.androidinterview.device.data

import com.example.androidinterview.core.network.safeApiCall
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.data.local.DeviceIdLocalDataSource
import com.example.androidinterview.device.data.remote.DeviceRemoteDataSource
import com.example.androidinterview.device.domain.DeviceRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [DeviceRepository] that fetches the identifier once and keeps it in local storage.
 *
 * A failed fetch is not stored, so the next call tries again.
 *
 * @param remote Remote source of the identifier.
 * @param local Local storage of the identifier.
 */
class DefaultDeviceRepository(
    private val remote: DeviceRemoteDataSource,
    private val local: DeviceIdLocalDataSource,
) : DeviceRepository {

    private val mutex = Mutex()

    override suspend fun getDeviceId(): DataResult<String> = mutex.withLock {
        local.get()?.let { return DataResult.Success(it) }
        val result = safeApiCall { remote.getDevice().deviceId }
        if (result is DataResult.Success) local.save(result.data)
        result
    }
}
