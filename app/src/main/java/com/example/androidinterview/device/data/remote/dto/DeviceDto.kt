package com.example.androidinterview.device.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/devices`.
 *
 * @param deviceId Stable identifier of this device.
 */
@Serializable
data class DeviceDto(
    @SerialName("device_id") val deviceId: String,
)
