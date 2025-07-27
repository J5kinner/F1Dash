package com.jskinner.f1dash.data.api

import com.jskinner.f1dash.data.models.*
import io.ktor.client.*

class OpenF1Api(httpClient: HttpClient) : BaseApiClient(httpClient) {

    suspend fun getPositions(
        sessionKey: Int,
        driverNumber: Int? = null,
        lapNumber: Int? = null
    ): ApiResult<List<OpenF1PositionResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }
        lapNumber?.let { parameters["lap_number"] = it }
        return makeRequest("position", parameters)
    }

    suspend fun getLaps(
        sessionKey: Int,
        driverNumber: Int? = null,
        lapNumber: Int? = null
    ): ApiResult<List<OpenF1LapResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }
        lapNumber?.let { parameters["lap_number"] = it }
        return makeRequest("laps", parameters)
    }

    suspend fun getIntervals(
        sessionKey: Int,
        driverNumber: Int? = null
    ): ApiResult<List<OpenF1IntervalResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }
        return makeRequest("intervals", parameters)
    }

    suspend fun getStints(
        sessionKey: Int,
        driverNumber: Int? = null
    ): ApiResult<List<OpenF1StintResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }
        return makeRequest("stints", parameters)
    }

    suspend fun getPitStops(
        sessionKey: Int,
        driverNumber: Int? = null
    ): ApiResult<List<OpenF1PitResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }
        return makeRequest("pit", parameters)
    }

    suspend fun getRaceReplayData(sessionKey: Int): ApiResult<OpenF1RaceReplayData> {
        return makeRequest("position", mapOf("session_key" to sessionKey)) {
            val positionsResult = getPositions(sessionKey)
            val lapsResult = getLaps(sessionKey)
            val intervalsResult = getIntervals(sessionKey)
            val stintsResult = getStints(sessionKey)
            val pitStopsResult = getPitStops(sessionKey)

            val positions = when (positionsResult) {
                is ApiResult.Success -> positionsResult.data
                else -> emptyList()
            }

            val laps = when (lapsResult) {
                is ApiResult.Success -> lapsResult.data
                else -> emptyList()
            }

            val intervals = when (intervalsResult) {
                is ApiResult.Success -> intervalsResult.data
                else -> emptyList()
            }

            val stints = when (stintsResult) {
                is ApiResult.Success -> stintsResult.data
                else -> emptyList()
            }

            val pitStops = when (pitStopsResult) {
                is ApiResult.Success -> pitStopsResult.data
                else -> emptyList()
            }

            OpenF1RaceReplayData(
                positions = positions,
                laps = laps,
                intervals = intervals,
                stints = stints,
                pitStops = pitStops
            )
        }
    }
}

data class OpenF1RaceReplayData(
    val positions: List<OpenF1PositionResponse>,
    val laps: List<OpenF1LapResponse>,
    val intervals: List<OpenF1IntervalResponse>,
    val stints: List<OpenF1StintResponse>,
    val pitStops: List<OpenF1PitResponse>
)