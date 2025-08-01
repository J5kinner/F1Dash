package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.MockReplayGenerator
import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.OpenF1Api
import com.jskinner.f1dash.data.api.getApiConfig
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.Flow

class F1RepositoryImpl(
    private val f1DriverApi: F1DriverApi,
    private val f1SessionApi: F1SessionApi,
    private val openF1Api: OpenF1Api
) : BaseRepository(), F1Repository {

    private val raceDataFetcher = RaceDataFetcher(f1DriverApi, f1SessionApi)
    private val apiConfig = getApiConfig()

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
            apiCall = { raceDataFetcher.fetchRaceData(sessionKey) }
        )

    override suspend fun getLatestRaceData(forceRefresh: Boolean): Flow<ApiResult<F1RaceData>> =
        handleApiCall(
            apiCall = { raceDataFetcher.fetchLatestRaceData(forceRefresh) }
        )

    override suspend fun getRaceReplay(sessionKey: Int): Flow<ApiResult<F1RaceReplay>> =
        handleApiCall(
            apiCall = { fetchRaceReplayData(sessionKey) }
        )

    private suspend fun fetchRaceReplayData(sessionKey: Int): ApiResult<F1RaceReplay> {
        return try {
            val session = getSessionForReplay(sessionKey)
            val drivers = getDriversForReplay(sessionKey)
            val raceReplay = generateRaceReplay(session, drivers, sessionKey)
            ApiResult.Success(raceReplay)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error fetching race replay data")
        }
    }

    private suspend fun getSessionForReplay(sessionKey: Int): F1Session {
        return when (val sessionResult = f1SessionApi.getSessionByKey(sessionKey)) {
            is ApiResult.Success -> sessionResult.data.toDomainModel()
            is ApiResult.Error -> throw Exception("Failed to get session: ${sessionResult.message}")
            is ApiResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    private suspend fun getDriversForReplay(sessionKey: Int): Map<Int, F1Driver> {
        return when (val driversResult = f1DriverApi.getDriversForSession(sessionKey)) {
            is ApiResult.Success -> driversResult.data.map { it.toDomainModel() }
                .associateBy { it.driverNumber }

            is ApiResult.Error -> throw Exception("Failed to get drivers: ${driversResult.message}")
            is ApiResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    private suspend fun generateRaceReplay(
        session: F1Session,
        drivers: Map<Int, F1Driver>,
        sessionKey: Int
    ): F1RaceReplay {
        return if (apiConfig.isDebugMode) {
            MockReplayGenerator.generateRaceReplay(session = session, drivers = drivers)
        } else {
            fetchRealRaceReplayWithFallback(session, drivers, sessionKey)
        }
    }

    private suspend fun fetchRealRaceReplayWithFallback(
        session: F1Session,
        drivers: Map<Int, F1Driver>,
        sessionKey: Int
    ): F1RaceReplay {
        return try {
            val raceReplayDataResult = openF1Api.getRaceReplayData(sessionKey)
            when (raceReplayDataResult) {
                is ApiResult.Success -> {
                    // TODO: Create proper transformer from OpenF1RaceReplayData to F1RaceReplay
                    // For now, fall back to mock data but use real session and drivers
                    MockReplayGenerator.generateRaceReplay(session = session, drivers = drivers)
                }

                is ApiResult.Error -> {
                    MockReplayGenerator.generateRaceReplay(session = session, drivers = drivers)
                }

                is ApiResult.Loading -> throw Exception("Loading state not expected")
            }
        } catch (e: Exception) {
            // Fall back to mock data if API call fails
            MockReplayGenerator.generateRaceReplay(session = session, drivers = drivers)
        }
    }
}