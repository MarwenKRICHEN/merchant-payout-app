package com.example.androidinterview.core.network

import kotlinx.serialization.Serializable

@Serializable
/**
 * Error body returned by the API on failures, e.g. `{"error":"Insufficient funds","code":"INSUFFICIENT_FUNDS"}`.
 *
 * @param error Human-readable message.
 * @param code Machine-readable error code.
 */
data class ApiErrorDto(
    val error: String,
    val code: String,
)
