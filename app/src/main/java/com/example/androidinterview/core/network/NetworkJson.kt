package com.example.androidinterview.core.network

import kotlinx.serialization.json.Json

/** Shared JSON configuration for every API call. */
val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
