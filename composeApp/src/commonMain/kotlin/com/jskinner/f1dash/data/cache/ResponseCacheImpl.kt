package com.jskinner.f1dash.data.cache

import com.jskinner.f1dash.data.models.ApiResult
import kotlinx.datetime.Clock

class ResponseCacheImpl<T> : ResponseCache<T> {
    override var cacheTimeInSeconds: Long = 10
    private val cache = mutableMapOf<String, CacheEntry<T>>()

    override suspend fun fetchResponse(
        key: String,
        forceRefresh: Boolean,
        fetcher: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        val cachedEntry = cache[key]
        val now = Clock.System.now().epochSeconds

        if (!forceRefresh && cachedEntry != null) {
            val ageInSeconds = now - cachedEntry.timestamp
            if (ageInSeconds < cacheTimeInSeconds) {
                return cachedEntry.data
            }
        }

        val result = try {
            fetcher()
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }

        if (result is ApiResult.Success) {
            cache[key] = CacheEntry(result, now)
        }

        return result
    }

    private data class CacheEntry<T>(
        val data: ApiResult<T>,
        val timestamp: Long
    )
}