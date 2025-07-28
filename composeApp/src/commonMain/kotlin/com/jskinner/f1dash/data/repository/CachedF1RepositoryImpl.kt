package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.OpenF1Api
import com.jskinner.f1dash.data.cache.ResponseCache
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.data.transformers.OpenF1DataTransformer
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CachedF1RepositoryImpl(
    private val f1DriverApi: F1DriverApi,
    private val f1SessionApi: F1SessionApi,
    private val openF1Api: OpenF1Api,
    private val raceDataCache: ResponseCache<F1RaceData>
) : BaseRepository(), F1Repository {

    private val raceDataFetcher = RaceDataFetcher(f1DriverApi, f1SessionApi)

    init {
        raceDataCache.cacheTimeInSeconds = 300
    }

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

    override suspend fun getRaceData(sessionKey: Int): Flow<ApiResult<F1RaceData>> = flow {
        emit(ApiResult.Loading)

        val result = raceDataCache.fetchResponse(
            key = "race_data_$sessionKey",
            forceRefresh = false
        ) {
            fetchRaceDataFromApi(sessionKey)
        }

        emit(result)
    }

    override suspend fun getLatestRaceData(forceRefresh: Boolean): Flow<ApiResult<F1RaceData>> = flow {
        emit(ApiResult.Loading)

        val result = raceDataCache.fetchResponse(
            key = "latest_race_data",
            forceRefresh = forceRefresh
        ) {
            fetchLatestRaceDataFromApi()
        }

        emit(result)
    }

    private suspend fun fetchRaceDataFromApi(sessionKey: Int): ApiResult<F1RaceData> {
        return raceDataFetcher.fetchRaceData(sessionKey)
    }

    private suspend fun fetchLatestRaceDataFromApi(): ApiResult<F1RaceData> {
        return raceDataFetcher.fetchLatestRaceData()
    }

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