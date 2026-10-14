package com.example.androidinterview.merchant.data

import com.example.androidinterview.core.network.HttpException
import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import com.example.androidinterview.merchant.data.remote.MerchantRemoteDataSource
import com.example.androidinterview.merchant.domain.MerchantRepository
import com.example.androidinterview.merchant.domain.model.Merchant
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * [MerchantRepository] backed by the remote API.
 *
 * @param remote Remote source of the merchant overview.
 */
class DefaultMerchantRepository(
    private val remote: MerchantRemoteDataSource,
) : MerchantRepository {

    override suspend fun getMerchant(): DataResult<Merchant> =
        try {
            DataResult.Success(remote.getMerchant().toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            DataResult.Failure(DataError.Network)
        } catch (e: HttpException) {
            DataResult.Failure(DataError.Server(e.code, e.error?.code))
        } catch (e: Exception) {
            DataResult.Failure(DataError.Unknown)
        }
}
