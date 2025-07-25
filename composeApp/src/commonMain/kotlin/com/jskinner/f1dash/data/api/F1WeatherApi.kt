package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1WeatherApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class F1WeatherApi(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.openf1.org/v1"
        private const val REQUEST_DELAY = 100L
    }
    
    suspend fun getWeather(sessionKey: Int): ApiResult<List<F1WeatherApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/weather") {
                parameter("session_key", sessionKey)
            }
            val weather = response.body<List<F1WeatherApiResponse>>()
            ApiResult.Success(weather)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getLatestWeather(sessionKey: Int): ApiResult<F1WeatherApiResponse> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/weather") {
                parameter("session_key", sessionKey)
            }
            val weather = response.body<List<F1WeatherApiResponse>>()
            if (weather.isNotEmpty()) {
                ApiResult.Success(weather.last())
            } else {
                ApiResult.Error("No weather data found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
}