package com.example.androidinterview.core.network

import com.example.androidinterview.core.result.DataError
import com.example.androidinterview.core.result.DataResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs an API call and maps any failure to a [DataError]; cancellation is always rethrown.
 *
 * Shared by the repositories so every feature reports failures the same way:
 * - [IOException] → [DataError.Network];
 * - [HttpException] → [DataError.Server] with the HTTP status and the API error code;
 * - anything else (e.g. an unexpected payload) → [DataError.Unknown].
 *
 * @param block Remote call and mapping to the domain model.
 */
suspend inline fun <T> safeApiCall(block: () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        DataResult.Failure(DataError.Network)
    } catch (e: HttpException) {
        DataResult.Failure(DataError.Server(e.code, e.error?.code))
    } catch (e: Exception) {
        DataResult.Failure(DataError.Unknown)
    }
