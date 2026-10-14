package com.example.androidinterview.core.result

/** Outcome of a data-layer call: a value or a [DataError]. */
sealed interface DataResult<out T> {
    /**
     * The call succeeded.
     *
     * @param data The result.
     */
    data class Success<T>(val data: T) : DataResult<T>

    /**
     * The call failed.
     *
     * @param error Why it failed.
     */
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/** Why a data-layer call failed. */
sealed interface DataError {
    /** No connectivity, timeout or any other transport failure. */
    data object Network : DataError

    /**
     * The server answered with a non-2xx status.
     *
     * @param code HTTP status code.
     * @param apiCode The API's machine-readable error code, if the body had one.
     */
    data class Server(val code: Int, val apiCode: String?) : DataError

    /** Unexpected payload or any other unrecognised failure. */
    data object Unknown : DataError
}
