package com.example.androidinterview.merchant.di

import com.example.androidinterview.merchant.data.DefaultMerchantRepository
import com.example.androidinterview.merchant.data.remote.KtorMerchantRemoteDataSource
import com.example.androidinterview.merchant.data.remote.MerchantRemoteDataSource
import com.example.androidinterview.merchant.domain.MerchantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/** Hilt bindings for the merchant feature. */
@Module
@InstallIn(SingletonComponent::class)
object MerchantModule {

    /**
     * Provides the remote data source.
     *
     * @param client Shared HTTP client.
     */
    @Provides
    @Singleton
    fun provideMerchantRemoteDataSource(client: HttpClient): MerchantRemoteDataSource =
        KtorMerchantRemoteDataSource(client)

    /**
     * Provides the repository.
     *
     * @param remote Remote source of the merchant overview.
     */
    @Provides
    @Singleton
    fun provideMerchantRepository(remote: MerchantRemoteDataSource): MerchantRepository =
        DefaultMerchantRepository(remote)
}
