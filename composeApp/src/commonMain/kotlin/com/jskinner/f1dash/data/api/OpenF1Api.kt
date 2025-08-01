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

        parameters["limit"] = 5000
        
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

        parameters["limit"] = 3000
        
        return makeRequest("laps", parameters)
    }

    suspend fun getIntervals(
        sessionKey: Int,
        driverNumber: Int? = null
    ): ApiResult<List<OpenF1IntervalResponse>> {
        val parameters = mutableMapOf<String, Any>("session_key" to sessionKey)
        driverNumber?.let { parameters["driver_number"] = it }

        parameters["limit"] = 2500
        
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
        return try {
            println("🏁 Fetching race replay data for session: $sessionKey")
            
            val positionsResult = getPositions(sessionKey)
            println(
                "📍 Positions result: ${
                    when (positionsResult) {
                        is ApiResult.Success -> "${positionsResult.data.size} entries"
                        is ApiResult.Error -> "ERROR: ${positionsResult.message}"
                        is ApiResult.Loading -> "LOADING"
                    }
                }"
            )
            
            val lapsResult = getLaps(sessionKey)
            println(
                "🏎️ Laps result: ${
                    when (lapsResult) {
                        is ApiResult.Success -> "${lapsResult.data.size} entries"
                        is ApiResult.Error -> "ERROR: ${lapsResult.message}"
                        is ApiResult.Loading -> "LOADING"
                    }
                }"
            )
            
            val intervalsResult = getIntervals(sessionKey)
            println(
                "⏱️ Intervals result: ${
                    when (intervalsResult) {
                        is ApiResult.Success -> "${intervalsResult.data.size} entries"
                        is ApiResult.Error -> "ERROR: ${intervalsResult.message}"
                        is ApiResult.Loading -> "LOADING"
                    }
                }"
            )
            
            val stintsResult = getStints(sessionKey)
            println(
                "🏃 Stints result: ${
                    when (stintsResult) {
                        is ApiResult.Success -> "${stintsResult.data.size} entries"
                        is ApiResult.Error -> "ERROR: ${stintsResult.message}"
                        is ApiResult.Loading -> "LOADING"
                    }
                }"
            )
            
            val pitStopsResult = getPitStops(sessionKey)
            println(
                "🔧 Pit stops result: ${
                    when (pitStopsResult) {
                        is ApiResult.Success -> "${pitStopsResult.data.size} entries"
                        is ApiResult.Error -> "ERROR: ${pitStopsResult.message}"
                        is ApiResult.Loading -> "LOADING"
                    }
                }"
            )

            val positions = when (positionsResult) {
                is ApiResult.Success -> positionsResult.data
                is ApiResult.Error -> {
                    println("❌ Positions API failed: ${positionsResult.message}")
                    emptyList()
                }

                is ApiResult.Loading -> emptyList()
            }

            val laps = when (lapsResult) {
                is ApiResult.Success -> lapsResult.data
                is ApiResult.Error -> {
                    println("❌ Laps API failed: ${lapsResult.message}")
                    emptyList()
                }

                is ApiResult.Loading -> emptyList()
            }

            val intervals = when (intervalsResult) {
                is ApiResult.Success -> intervalsResult.data
                is ApiResult.Error -> {
                    println("❌ Intervals API failed: ${intervalsResult.message}")
                    emptyList()
                }

                is ApiResult.Loading -> emptyList()
            }

            val stints = when (stintsResult) {
                is ApiResult.Success -> stintsResult.data
                is ApiResult.Error -> {
                    println("❌ Stints API failed: ${stintsResult.message}")
                    emptyList()
                }

                is ApiResult.Loading -> emptyList()
            }

            val pitStops = when (pitStopsResult) {
                is ApiResult.Success -> pitStopsResult.data
                is ApiResult.Error -> {
                    println("❌ Pit stops API failed: ${pitStopsResult.message}")
                    emptyList()
                }

                is ApiResult.Loading -> emptyList()
            }

            val raceReplayData = OpenF1RaceReplayData(
//                positions = positions,
                laps = laps,
                intervals = intervals,
                stints = stints,
                pitStops = pitStops
            )

            println("✅ Race replay data compiled successfully")
            ApiResult.Success(raceReplayData)

        } catch (e: Exception) {
            println("❌ Failed to get race replay data: ${e.message}")
            ApiResult.Error("Failed to get race replay data: ${e.message}")
        }
    }
}

data class OpenF1RaceReplayData(
//    val positions: List<OpenF1PositionResponse>,
    val laps: List<OpenF1LapResponse>,
    val intervals: List<OpenF1IntervalResponse>,
    val stints: List<OpenF1StintResponse>,
    val pitStops: List<OpenF1PitResponse>
)