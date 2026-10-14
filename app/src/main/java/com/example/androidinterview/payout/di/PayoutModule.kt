package com.example.androidinterview.payout.di

import com.example.androidinterview.payout.data.DefaultPayoutRepository
import com.example.androidinterview.payout.data.remote.KtorPayoutRemoteDataSource
import com.example.androidinterview.payout.data.remote.PayoutRemoteDataSource
import com.example.androidinterview.payout.domain.PayoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/** Hilt bindings for the payout feature. */
@Module
@InstallIn(SingletonComponent::class)
object PayoutModule {

    /**
     * Provides the remote data source.
     *
     * @param client Shared HTTP client.
     */
    @Provides
    @Singleton
    fun providePayoutRemoteDataSource(client: HttpClient): PayoutRemoteDataSource =
        KtorPayoutRemoteDataSource(client)

    /**
     * Provides the repository.
     *
     * @param remote Remote source of payouts.
     */
    @Provides
    @Singleton
    fun providePayoutRepository(remote: PayoutRemoteDataSource): PayoutRepository =
        DefaultPayoutRepository(remote)
}
