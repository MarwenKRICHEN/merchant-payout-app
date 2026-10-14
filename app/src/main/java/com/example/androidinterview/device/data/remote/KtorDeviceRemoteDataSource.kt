package com.example.androidinterview.device.data.remote

import com.example.androidinterview.core.network.getJson
import com.example.androidinterview.device.data.remote.dto.DeviceDto
import io.ktor.client.HttpClient

private const val DEVICES_PATH = "api/devices"

/**
 * [DeviceRemoteDataSource] that calls the API with a Ktor [HttpClient].
 *
 * @param client Client configured with the API base URL.
 */
class KtorDeviceRemoteDataSource(
    private val client: HttpClient,
) : DeviceRemoteDataSource {

    override suspend fun getDevice(): DeviceDto = client.getJson(DEVICES_PATH)
}
