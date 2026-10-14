package com.example.androidinterview.core.network

/**
 * Thrown when the API answers with a non-2xx status code.
 *
 * @param code HTTP status code.
 * @param error Parsed error body, or null when the body was missing or not the API's error format.
 */
class HttpException(
    val code: Int,
    val error: ApiErrorDto?,
) : Exception("HTTP $code${error?.let { ": ${it.code}" }.orEmpty()}")
