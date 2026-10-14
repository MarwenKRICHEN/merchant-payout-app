package com.example.androidinterview.device.data

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.device.data.local.DeviceIdLocalDataSource
import com.example.androidinterview.device.data.remote.DeviceRemoteDataSource
import com.example.androidinterview.device.data.remote.dto.DeviceDto
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultDeviceRepositoryTest {

    private class FakeLocal(var stored: String? = null) : DeviceIdLocalDataSource {
        override suspend fun get(): String? = stored

        override suspend fun save(id: String) {
            stored = id
        }
    }

    private class FakeRemote(var outcome: () -> DeviceDto) : DeviceRemoteDataSource {
        var calls = 0

        override suspend fun getDevice(): DeviceDto {
            calls++
            yield()
            return outcome()
        }
    }

    private val remote = FakeRemote { DeviceDto("device_1") }
    private val local = FakeLocal()
    private val repository = DefaultDeviceRepository(remote, local)

    @Test
    fun `first call fetches the id and stores it, later calls reuse it`() = runTest {
        assertEquals(DataResult.Success("device_1"), repository.getDeviceId())
        assertEquals(DataResult.Success("device_1"), repository.getDeviceId())

        assertEquals(1, remote.calls)
        assertEquals("device_1", local.stored)
    }

    @Test
    fun `a stored id is used without a request`() = runTest {
        local.stored = "device_previous"

        assertEquals(DataResult.Success("device_previous"), repository.getDeviceId())
        assertEquals(0, remote.calls)
    }

    @Test
    fun `a failure is returned and not stored, so the next call retries`() = runTest {
        remote.outcome = { throw IOException() }

        assertEquals(DataResult.Failure(DataError.Network), repository.getDeviceId())
        assertNull(local.stored)

        remote.outcome = { DeviceDto("device_2") }
        assertEquals(DataResult.Success("device_2"), repository.getDeviceId())
        assertEquals(2, remote.calls)
    }

    @Test
    fun `concurrent calls fetch the id once`() = runTest {
        val results = (1..3).map { async { repository.getDeviceId() } }.awaitAll()

        assertEquals(List(3) { DataResult.Success("device_1") }, results)
        assertEquals(1, remote.calls)
    }
}
