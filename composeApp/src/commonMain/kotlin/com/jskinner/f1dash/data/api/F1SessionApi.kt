package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.models.F1SessionApiResponse
import com.jskinner.f1dash.data.models.F1SessionResultApiResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class F1SessionApi(httpClient: HttpClient) : BaseApiClient(httpClient) {
    
    suspend fun getSessions(year: Int? = null): ApiResult<List<F1SessionApiResponse>> {
        val parameters = mutableMapOf<String, Any>()
        year?.let { parameters["year"] = it }
        return makeRequest("sessions", parameters)
    }
    
    suspend fun getLatestSession(): ApiResult<F1SessionApiResponse> {
        return makeRequest("sessions", mapOf("year" to 2024)) {
            val sessions = getSessions(2024)
            val sessionsList = when (sessions) {
                is ApiResult.Success -> sessions.data
                else -> emptyList()
            }

            if (sessionsList.isNotEmpty()) {
                sessionsList.last()
            } else {
                throw Exception("No sessions found")
            }
        }
    }
    
    suspend fun getLatestRaceSession(): ApiResult<F1SessionApiResponse> {
        return makeRequest("sessions", mapOf("year" to 2025, "session_type" to "Race")) {
            val raceSessions = findRaceSessionsInYear(2025)
                ?: findRaceSessionsInYear(2024)
                ?: emptyList()

            raceSessions.lastOrNull()
                ?: throw Exception("No race sessions found for 2024 or 2025")
        }
    }

    private suspend fun findRaceSessionsInYear(year: Int): List<F1SessionApiResponse>? {
        return when (val sessions = getSessions(year)) {
            is ApiResult.Success -> sessions.data.filter { isRaceSession(it) }
            else -> null
        }
    }

    private fun isRaceSession(session: F1SessionApiResponse): Boolean {
        return session.sessionName?.contains("Race", ignoreCase = true) == true ||
                session.sessionType?.contains("Race", ignoreCase = true) == true
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