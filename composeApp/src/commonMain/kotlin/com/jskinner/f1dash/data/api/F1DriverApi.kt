package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1DriverApiResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class F1DriverApi(httpClient: HttpClient) : BaseApiClient(httpClient) {
    
    suspend fun getDrivers(): ApiResult<List<F1DriverApiResponse>> {
        return makeRequest("drivers")
    }
    
    suspend fun getDriversForSession(sessionKey: Int): ApiResult<List<F1DriverApiResponse>> {
        return makeRequest("drivers", mapOf("session_key" to sessionKey))
    }
    
    suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int? = null): ApiResult<F1DriverApiResponse> {
        val parameters = mutableMapOf("driver_number" to driverNumber)
        sessionKey?.let { parameters["session_key"] = it }

        return makeRequest("drivers", parameters) {
            val drivers = httpClient.get("$BASE_URL/drivers") {
                parameters.forEach { (key, value) ->
                    parameter(key, value.toString())
                }
            }.body<List<F1DriverApiResponse>>()
            
            if (drivers.isNotEmpty()) {
                drivers.first()
            } else {
                throw Exception("Driver not found")
            }
        }
    }
}