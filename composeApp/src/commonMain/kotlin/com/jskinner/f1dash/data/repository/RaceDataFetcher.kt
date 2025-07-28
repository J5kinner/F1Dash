package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.*

class RaceDataFetcher(
    private val f1DriverApi: F1DriverApi,
    private val f1SessionApi: F1SessionApi
) {

    suspend fun fetchRaceData(sessionKey: Int): ApiResult<F1RaceData> {
        return try {
            val session = getSessionData()
            val drivers = getDriversData(sessionKey)
            val sessionResults = getSessionResults(sessionKey)

            val driverResults = createDriverResults(drivers, sessionResults)

            ApiResult.Success(
                F1RaceData(
                    session = session,
                    driverResults = driverResults,
                    fastestLap = null
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error fetching race data")
        }
    }

    suspend fun fetchLatestRaceData(): ApiResult<F1RaceData> {
        return try {
            val session = getLatestRaceSession()
            val sessionKey = session.sessionKey
            val drivers = getDriversData(sessionKey)
            val sessionResults = getSessionResults(sessionKey)

            val driverResults = createDriverResults(drivers, sessionResults)

            ApiResult.Success(
                F1RaceData(
                    session = session,
                    driverResults = driverResults,
                    fastestLap = null
                )
            )
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error fetching latest race data")
        }
    }

    private suspend fun getSessionData(): F1Session {
        return when (val result = f1SessionApi.getLatestSession()) {
            is ApiResult.Success -> result.data.toDomainModel()
            is ApiResult.Error -> throw Exception(result.message)
            is ApiResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    private suspend fun getLatestRaceSession(): F1Session {
        return when (val result = f1SessionApi.getLatestRaceSession()) {
            is ApiResult.Success -> result.data.toDomainModel()
            is ApiResult.Error -> throw Exception("Failed to get latest race session: ${result.message}")
            is ApiResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    private suspend fun getDriversData(sessionKey: Int): List<F1Driver> {
        return when (val result = f1DriverApi.getDriversForSession(sessionKey)) {
            is ApiResult.Success -> result.data.map { it.toDomainModel() }
            is ApiResult.Error -> throw Exception("Failed to get drivers: ${result.message}")
            is ApiResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    private suspend fun getSessionResults(sessionKey: Int): List<F1SessionResult> {
        return when (val result = f1SessionApi.getSessionResults(sessionKey)) {
            is ApiResult.Success -> result.data.map { it.toDomainModel() }
            is ApiResult.Error -> emptyList()
            is ApiResult.Loading -> emptyList()
        }
    }

    private fun createDriverResults(
        drivers: List<F1Driver>,
        sessionResults: List<F1SessionResult>
    ): List<F1DriverResult> {
        return drivers.mapIndexed { index, driver ->
            val position = sessionResults.find { it.driverNumber == driver.driverNumber }?.position
                ?: (index + 1)

            F1DriverResult(
                driver = driver,
                position = position,
                lapTime = "--:--",
                currentTyre = "UNKNOWN"
            )
        }.sortedBy { it.position }
    }
}