package com.example.androidinterview.device.di

import android.content.Context
import com.example.androidinterview.device.data.DefaultDeviceRepository
import com.example.androidinterview.device.data.local.DeviceIdLocalDataSource
import com.example.androidinterview.device.data.local.SharedPreferencesDeviceIdLocalDataSource
import com.example.androidinterview.device.data.remote.DeviceRemoteDataSource
import com.example.androidinterview.device.data.remote.KtorDeviceRemoteDataSource
import com.example.androidinterview.device.domain.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

private const val DEVICE_PREFERENCES = "device"

/** Hilt bindings for the device identity feature. */
@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {

    /**
     * Provides the remote data source.
     *
     * @param client Shared HTTP client.
     */
    @Provides
    @Singleton
    fun provideDeviceRemoteDataSource(client: HttpClient): DeviceRemoteDataSource =
        KtorDeviceRemoteDataSource(client)

    /**
     * Provides the local storage of the identifier.
     *
     * @param context Application context owning the preferences file.
     */
    @Provides
    @Singleton
    fun provideDeviceIdLocalDataSource(@ApplicationContext context: Context): DeviceIdLocalDataSource =
        SharedPreferencesDeviceIdLocalDataSource(
            context.getSharedPreferences(DEVICE_PREFERENCES, Context.MODE_PRIVATE),
        )

    /**
     * Provides the repository.
     *
     * @param remote Remote source of the identifier.
     * @param local Local storage of the identifier.
     */
    @Provides
    @Singleton
    fun provideDeviceRepository(
        remote: DeviceRemoteDataSource,
        local: DeviceIdLocalDataSource,
    ): DeviceRepository = DefaultDeviceRepository(remote, local)
}
