package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1SessionApiResponse
import com.jskinner.f1dash.data.models.F1SessionResultApiResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class F1SessionApi(httpClient: HttpClient) : BaseApiClient(httpClient) {
    
    suspend fun getSessions(year: Int? = null): ApiResult<List<F1SessionApiResponse>> {
        val parameters = mutableMapOf<String, Any>()
        year?.let { parameters["year"] = it }
        return makeRequest("sessions", parameters)
    }
    
    suspend fun getLatestSession(): ApiResult<F1SessionApiResponse> {
        return makeRequest("sessions", mapOf("year" to 2024)) {
            val sessions = httpClient.get("$BASE_URL/sessions") {
                parameter("year", 2024)
            }.body<List<F1SessionApiResponse>>()
            
            if (sessions.isNotEmpty()) {
                sessions.last()
            } else {
                throw Exception("No sessions found")
            }
        }
    }
    
    suspend fun getLatestRaceSession(): ApiResult<F1SessionApiResponse> {
        return makeRequest("sessions", mapOf("year" to 2025, "session_type" to "Race")) {
            var sessions = httpClient.get("$BASE_URL/sessions") {
                parameter("year", 2025)
                parameter("session_type", "Race")
            }.body<List<F1SessionApiResponse>>()
            
            if (sessions.isEmpty()) {
                sessions = httpClient.get("$BASE_URL/sessions") {
                    parameter("year", 2024)
                    parameter("session_type", "Race")
                }.body<List<F1SessionApiResponse>>()
            }
            
            if (sessions.isNotEmpty()) {
                sessions.last()
            } else {
                throw Exception("No race sessions found for 2024 or 2025")
            }
        }
    }

    suspend fun getSessionByKey(sessionKey: Int): ApiResult<F1SessionApiResponse> {
        return makeRequest("sessions", mapOf("session_key" to sessionKey)) {
            val sessions = httpClient.get("$BASE_URL/sessions") {
                parameter("session_key", sessionKey)
            }.body<List<F1SessionApiResponse>>()

            if (sessions.isNotEmpty()) {
                sessions.first()
            } else {
                throw Exception("Session not found for key: $sessionKey")
            }
        }
    }

    suspend fun getSessionResults(sessionKey: Int): ApiResult<List<F1SessionResultApiResponse>> {
        return makeRequest("session_result", mapOf("session_key" to sessionKey))
    }
}