package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1StintApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class F1StintApi(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.openf1.org/v1"
        private const val REQUEST_DELAY = 100L
    }
    
    suspend fun getStints(sessionKey: Int): ApiResult<List<F1StintApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/stints") {
                parameter("session_key", sessionKey)
            }
            val stints = response.body<List<F1StintApiResponse>>()
            ApiResult.Success(stints)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
}