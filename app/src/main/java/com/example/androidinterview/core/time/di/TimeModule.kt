package com.example.androidinterview.core.time.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/** Hilt bindings for time, so "now" can be fixed in tests. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    /** Provides the system clock in the device time zone. */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
