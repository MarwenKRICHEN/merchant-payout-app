package com.example.androidinterview.authentication.di

import android.content.Context
import com.example.androidinterview.authentication.AuthenticationBridge
import com.example.androidinterview.authentication.AuthenticationProvider
import com.example.androidinterview.authentication.biometric.BiometricAuthenticationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for user authentication. */
@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {

    /** Provides the bridge shared by the provider and the UI that shows the prompt. */
    @Provides
    @Singleton
    fun provideAuthenticationBridge(): AuthenticationBridge = AuthenticationBridge()

    /**
     * Provides the biometric authentication.
     *
     * @param context Application context.
     * @param bridge Shows the prompt in the foreground Activity.
     */
    @Provides
    @Singleton
    fun provideAuthenticationProvider(
        @ApplicationContext context: Context,
        bridge: AuthenticationBridge,
    ): AuthenticationProvider = BiometricAuthenticationProvider(context, bridge)
}
