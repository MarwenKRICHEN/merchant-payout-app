package com.example.androidinterview.device.domain

import com.example.androidinterview.core.result.DataResult

/** Source of the identifier of this device. */
interface DeviceRepository {

    /**
     * Returns the stable device identifier: the stored one, or the one fetched from the API the
     * first time (then stored for the next launches).
     */
    suspend fun getDeviceId(): DataResult<String>
}
