package com.example.androidinterview.core.network.di

import com.example.androidinterview.core.network.createHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/** Hilt bindings for networking. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Provides the app-wide HTTP client. */
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = createHttpClient()
}
