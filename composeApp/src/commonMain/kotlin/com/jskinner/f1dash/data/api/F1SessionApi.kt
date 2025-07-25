package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1SessionApiResponse
import com.jskinner.f1dash.data.models.F1SessionResultApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class F1SessionApi(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.openf1.org/v1"
        private const val REQUEST_DELAY = 100L
    }
    
    suspend fun getSessions(year: Int? = null): ApiResult<List<F1SessionApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/sessions") {
                year?.let { parameter("year", it) }
            }
            val sessions = response.body<List<F1SessionApiResponse>>()
            ApiResult.Success(sessions)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getLatestSession(): ApiResult<F1SessionApiResponse> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/sessions") {
                parameter("year", 2024)
            }
            val sessions = response.body<List<F1SessionApiResponse>>()
            if (sessions.isNotEmpty()) {
                ApiResult.Success(sessions.last())
            } else {
                ApiResult.Error("No sessions found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getLatestRaceSession(): ApiResult<F1SessionApiResponse> {
        return try {
            delay(REQUEST_DELAY)
            
            var response = httpClient.get("$BASE_URL/sessions") {
                parameter("year", 2025)
                parameter("session_type", "Race")
            }
            var sessions = response.body<List<F1SessionApiResponse>>()
            
            if (sessions.isEmpty()) {
                response = httpClient.get("$BASE_URL/sessions") {
                    parameter("year", 2024)
                    parameter("session_type", "Race")
                }
                sessions = response.body<List<F1SessionApiResponse>>()
            }
            
            if (sessions.isNotEmpty()) {
                ApiResult.Success(sessions.last())
            } else {
                ApiResult.Error("No race sessions found for 2024 or 2025")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getSessionResults(sessionKey: Int): ApiResult<List<F1SessionResultApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/session_result") {
                parameter("session_key", sessionKey)
            }
            val results = response.body<List<F1SessionResultApiResponse>>()
            ApiResult.Success(results)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
}