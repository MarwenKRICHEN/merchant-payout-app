package com.example.androidinterview.device.data.local

/** Local storage of the device identifier. */
interface DeviceIdLocalDataSource {

    /** Returns the stored identifier, or null when none was saved yet. */
    suspend fun get(): String?

    /**
     * Stores the identifier.
     *
     * @param id Device identifier to keep across launches.
     */
    suspend fun save(id: String)
}
