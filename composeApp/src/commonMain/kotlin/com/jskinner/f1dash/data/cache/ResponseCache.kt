package com.jskinner.f1dash.data.cache

import com.jskinner.f1dash.data.models.ApiResult

interface ResponseCache<T> {
    var cacheTimeInSeconds: Long
    suspend fun fetchResponse(
        key: String,
        forceRefresh: Boolean = false,
        fetcher: suspend () -> ApiResult<T>
    ): ApiResult<T>
}