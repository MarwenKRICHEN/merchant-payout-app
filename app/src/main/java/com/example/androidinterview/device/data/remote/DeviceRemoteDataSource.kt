package com.example.androidinterview.device.data.remote

import com.example.androidinterview.device.data.remote.dto.DeviceDto

/** Remote source of the device identity. */
interface DeviceRemoteDataSource {

    /**
     * `GET /api/devices`: returns the identifier of this device.
     *
     * @throws java.io.IOException on network failure
     * @throws com.example.androidinterview.core.network.HttpException on a non-2xx response
     * @throws kotlinx.serialization.SerializationException on an unexpected payload
     */
    suspend fun getDevice(): DeviceDto
}
