package com.example.androidinterview.device.domain

import com.example.androidinterview.core.result.DataResult

/**
 * Scripted [DeviceRepository].
 *
 * @param result Returned by every [getDeviceId] call.
 */
class FakeDeviceRepository(
    var result: DataResult<String> = DataResult.Success("device_1"),
) : DeviceRepository {

    override suspend fun getDeviceId(): DataResult<String> = result
}
