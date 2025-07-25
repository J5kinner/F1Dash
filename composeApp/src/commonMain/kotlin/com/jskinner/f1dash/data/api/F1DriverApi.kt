package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1DriverApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class F1DriverApi(private val httpClient: HttpClient) {
    
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
}