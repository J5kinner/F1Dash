package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class F1ApiClient(private val httpClient: HttpClient) {
    
    companion object {
        private const val BASE_URL = "https://api.openf1.org/v1"
        private const val REQUEST_DELAY = 100L
    }
    
    suspend fun getDrivers(): ApiResult<List<F1DriverApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/drivers")
            val drivers = response.body<List<F1DriverApiResponse>>()
            ApiResult.Success(drivers)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getDriversForSession(sessionKey: Int): ApiResult<List<F1DriverApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/drivers") {
                parameter("session_key", sessionKey)
            }
            val drivers = response.body<List<F1DriverApiResponse>>()
            ApiResult.Success(drivers)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int? = null): ApiResult<F1DriverApiResponse> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/drivers") {
                parameter("driver_number", driverNumber)
                sessionKey?.let { parameter("session_key", it) }
            }
            val drivers = response.body<List<F1DriverApiResponse>>()
            if (drivers.isNotEmpty()) {
                ApiResult.Success(drivers.first())
            } else {
                ApiResult.Error("Driver not found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
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
                // Return the latest session (sessions are typically ordered by date)
                ApiResult.Success(sessions.last())
            } else {
                ApiResult.Error("No sessions found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getLaps(sessionKey: Int): ApiResult<List<F1LapApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/laps") {
                parameter("session_key", sessionKey)
            }
            val laps = response.body<List<F1LapApiResponse>>()
            ApiResult.Success(laps)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
    
    suspend fun getPositions(sessionKey: Int): ApiResult<List<F1PositionApiResponse>> {
        return try {
            delay(REQUEST_DELAY)
            val response = httpClient.get("$BASE_URL/position") {
                parameter("session_key", sessionKey)
            }
            val positions = response.body<List<F1PositionApiResponse>>()
            ApiResult.Success(positions)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
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
                // Return the latest weather data
                ApiResult.Success(weather.last())
            } else {
                ApiResult.Error("No weather data found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error occurred")
        }
    }
}