package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.OpenF1Api
import com.jskinner.f1dash.data.transformers.OpenF1DataTransformer
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.Flow

class F1RepositoryImpl(
    private val f1DriverApi: F1DriverApi,
    private val f1SessionApi: F1SessionApi,
    private val openF1Api: OpenF1Api
) : BaseRepository(), F1Repository {

    override suspend fun getDrivers(): Flow<ApiResult<List<F1Driver>>> =
        handleApiCallWithTransform(
            apiCall = { f1DriverApi.getDrivers() },
            transform = { drivers ->
                drivers.distinctBy { it.driverNumber }
                    .map { it.toDomainModel() }
                    .sortedBy { it.driverNumber }
            }
        )

    override suspend fun getDriversForSession(sessionKey: Int): Flow<ApiResult<List<F1Driver>>> =
        handleApiCallWithTransform(
            apiCall = { f1DriverApi.getDriversForSession(sessionKey) },
            transform = { drivers ->
                drivers.map { it.toDomainModel() }.sortedBy { it.driverNumber }
            }
        )

    override suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int?): Flow<ApiResult<F1Driver>> =
        handleApiCallWithTransform(
            apiCall = { f1DriverApi.getDriverByNumber(driverNumber, sessionKey) },
            transform = { driver -> driver.toDomainModel() }
        )

    override suspend fun getSessions(year: Int?): Flow<ApiResult<List<F1Session>>> =
        handleApiCallWithTransform(
            apiCall = { f1SessionApi.getSessions(year) },
            transform = { sessions -> sessions.map { it.toDomainModel() } }
        )

    override suspend fun getLatestSession(): Flow<ApiResult<F1Session>> =
        handleApiCallWithTransform(
            apiCall = { f1SessionApi.getLatestSession() },
            transform = { session -> session.toDomainModel() }
        )

    override suspend fun getLatestRaceSession(): Flow<ApiResult<F1Session>> =
        handleApiCallWithTransform(
            apiCall = { f1SessionApi.getLatestRaceSession() },
            transform = { session -> session.toDomainModel() }
        )

    override suspend fun getRaceData(sessionKey: Int): Flow<ApiResult<F1RaceData>> =
        handleApiCall(
            apiCall = {
                try {
                    val sessionResult = f1SessionApi.getLatestSession()
                    val driversResult = f1DriverApi.getDriversForSession(sessionKey)
                    val sessionResultsResult = f1SessionApi.getSessionResults(sessionKey)

                    val session = when (sessionResult) {
                        is ApiResult.Success -> sessionResult.data.toDomainModel()
                        is ApiResult.Error -> throw Exception(sessionResult.message)
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val drivers = when (driversResult) {
                        is ApiResult.Success -> driversResult.data.map { it.toDomainModel() }
                        is ApiResult.Error -> throw Exception(driversResult.message)
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val sessionResults = when (sessionResultsResult) {
                        is ApiResult.Success -> sessionResultsResult.data.map { it.toDomainModel() }
                        is ApiResult.Error -> emptyList()
                        is ApiResult.Loading -> emptyList()
                    }

                    val driverResults = drivers.mapIndexed { index, driver ->
                        val position = sessionResults.find { it.driverNumber == driver.driverNumber }?.position
                            ?: (index + 1)

                        F1DriverResult(
                            driver = driver,
                            position = position,
                            lapTime = "--:--",
                            currentTyre = "UNKNOWN"
                        )
                    }.sortedBy { it.position }

                    ApiResult.Success(
                        F1RaceData(
                            session = session,
                            driverResults = driverResults,
                            fastestLap = null
                        )
                    )
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Error fetching race data")
                }
            }
        )

    override suspend fun getLatestRaceData(): Flow<ApiResult<F1RaceData>> =
        handleApiCall(
            apiCall = {
                try {
                    val sessionResult = f1SessionApi.getLatestRaceSession()
                    val session = when (sessionResult) {
                        is ApiResult.Success -> sessionResult.data.toDomainModel()
                        is ApiResult.Error -> throw Exception("Failed to get latest race session: ${sessionResult.message}")
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val sessionKey = session.sessionKey
                    val driversResult = f1DriverApi.getDriversForSession(sessionKey)
                    val sessionResultsResult = f1SessionApi.getSessionResults(sessionKey)

                    val drivers = when (driversResult) {
                        is ApiResult.Success -> driversResult.data.map { it.toDomainModel() }
                        is ApiResult.Error -> throw Exception("Failed to get drivers: ${driversResult.message}")
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val sessionResults = when (sessionResultsResult) {
                        is ApiResult.Success -> sessionResultsResult.data.map { it.toDomainModel() }
                        is ApiResult.Error -> emptyList()
                        is ApiResult.Loading -> emptyList()
                    }

                    val driverResults = drivers.mapIndexed { index, driver ->
                        val position = sessionResults.find { it.driverNumber == driver.driverNumber }?.position
                            ?: (index + 1)

                        F1DriverResult(
                            driver = driver,
                            position = position,
                            lapTime = "--:--",
                            currentTyre = "UNKNOWN"
                        )
                    }.sortedBy { it.position }

                    ApiResult.Success(
                        F1RaceData(
                            session = session,
                            driverResults = driverResults,
                            fastestLap = null
                        )
                    )
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Error fetching latest race data")
                }
            }
        )

    override suspend fun getRaceReplay(sessionKey: Int): Flow<ApiResult<F1RaceReplay>> =
        handleApiCall(
            apiCall = {
                try {
                    val sessionResult = f1SessionApi.getSessionByKey(sessionKey)
                    val session = when (sessionResult) {
                        is ApiResult.Success -> sessionResult.data.toDomainModel()
                        is ApiResult.Error -> throw Exception("Failed to get session: ${sessionResult.message}")
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val driversResult = f1DriverApi.getDriversForSession(sessionKey)
                    val drivers = when (driversResult) {
                        is ApiResult.Success -> driversResult.data.map { it.toDomainModel() }
                            .associateBy { it.driverNumber }

                        is ApiResult.Error -> throw Exception("Failed to get drivers: ${driversResult.message}")
                        is ApiResult.Loading -> throw Exception("Loading state not expected")
                    }

                    val openF1Result = openF1Api.getRaceReplayData(sessionKey)
                    when (openF1Result) {
                        is ApiResult.Success -> {
                            ApiResult.Success(
                                OpenF1DataTransformer.transformToRaceReplay(
                                    openF1Data = openF1Result.data,
                                    session = session,
                                    drivers = drivers
                                )
                            )
                        }

                        is ApiResult.Error -> {
                            throw Exception("Failed to get race replay data: ${openF1Result.message}")
                        }

                        is ApiResult.Loading -> {
                            throw Exception("Loading state not expected")
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message ?: "Error fetching race replay data")
                }
            }
        )
}