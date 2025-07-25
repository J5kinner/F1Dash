package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1DriverApi
import com.jskinner.f1dash.data.api.F1LapApi
import com.jskinner.f1dash.data.api.F1PositionApi
import com.jskinner.f1dash.data.api.F1SessionApi
import com.jskinner.f1dash.data.api.F1StintApi
import com.jskinner.f1dash.data.api.F1WeatherApi
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class F1RepositoryImpl(
    private val f1DriverApi: F1DriverApi,
    private val f1LapApi: F1LapApi,
    private val f1PositionApi: F1PositionApi,
    private val f1SessionApi: F1SessionApi,
    private val f1StintApi: F1StintApi,
    private val f1WeatherApi: F1WeatherApi,

    ) : F1Repository {
    
    override suspend fun getDrivers(): Flow<ApiResult<List<F1Driver>>> = flow {
        emit(ApiResult.Loading)
        val result = f1DriverApi.getDrivers()
        when (result) {
            is ApiResult.Success -> {
                val drivers = result.data
                    .distinctBy { it.driverNumber } // Remove duplicates based on driver number
                    .map { it.toDomainModel() }
                    .sortedBy { it.driverNumber }
                emit(ApiResult.Success(drivers))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getDriversForSession(sessionKey: Int): Flow<ApiResult<List<F1Driver>>> = flow {
        emit(ApiResult.Loading)
        val result = f1DriverApi.getDriversForSession(sessionKey)
        when (result) {
            is ApiResult.Success -> {
                val drivers = result.data
                    .map { it.toDomainModel() }
                    .sortedBy { it.driverNumber }
                emit(ApiResult.Success(drivers))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int?): Flow<ApiResult<F1Driver>> = flow {
        emit(ApiResult.Loading)
        val result = f1DriverApi.getDriverByNumber(driverNumber, sessionKey)
        when (result) {
            is ApiResult.Success -> {
                val driver = result.data.toDomainModel()
                emit(ApiResult.Success(driver))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getSessions(year: Int?): Flow<ApiResult<List<F1Session>>> = flow {
        emit(ApiResult.Loading)
        val result = f1SessionApi.getSessions(year)
        when (result) {
            is ApiResult.Success -> {
                val sessions = result.data.map { it.toDomainModel() }
                emit(ApiResult.Success(sessions))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getLatestSession(): Flow<ApiResult<F1Session>> = flow {
        emit(ApiResult.Loading)
        val result = f1SessionApi.getLatestSession()
        when (result) {
            is ApiResult.Success -> {
                val session = result.data.toDomainModel()
                emit(ApiResult.Success(session))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getLatestRaceSession(): Flow<ApiResult<F1Session>> = flow {
        emit(ApiResult.Loading)
        val result = f1SessionApi.getLatestRaceSession()
        when (result) {
            is ApiResult.Success -> {
                val session = result.data.toDomainModel()
                emit(ApiResult.Success(session))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
    
    override suspend fun getRaceData(sessionKey: Int): Flow<ApiResult<F1RaceData>> = flow {
        emit(ApiResult.Loading)
        
        try {
            // Fetch all required data concurrently
            val sessionResult = f1SessionApi.getLatestSession()
            val driversResult = f1DriverApi.getDriversForSession(sessionKey)
            val lapsResult = f1LapApi.getLaps(sessionKey)
            val positionsResult = f1PositionApi.getPositions(sessionKey)
            val weatherResult = f1WeatherApi.getLatestWeather(sessionKey)
            val stintsResult = f1StintApi.getStints(sessionKey)
            
            // Process session data
            val session = when (sessionResult) {
                is ApiResult.Success -> sessionResult.data.toDomainModel()
                is ApiResult.Error -> {
                    emit(ApiResult.Error(sessionResult.message))
                    return@flow
                }
                is ApiResult.Loading -> {
                    emit(ApiResult.Loading)
                    return@flow
                }
            }
            
            // Process drivers data
            val drivers = when (driversResult) {
                is ApiResult.Success -> driversResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> {
                    emit(ApiResult.Error(driversResult.message))
                    return@flow
                }
                is ApiResult.Loading -> {
                    emit(ApiResult.Loading)
                    return@flow
                }
            }
            
            // Process laps data
            val laps = when (lapsResult) {
                is ApiResult.Success -> lapsResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> emptyList()
                is ApiResult.Loading -> emptyList()
            }
            
            // Process positions data
            val positions = when (positionsResult) {
                is ApiResult.Success -> positionsResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> emptyList()
                is ApiResult.Loading -> emptyList()
            }
            
            // Process weather data
            val weather = when (weatherResult) {
                is ApiResult.Success -> weatherResult.data.toDomainModel()
                is ApiResult.Error -> null
                is ApiResult.Loading -> null
            }
            
            // Process stint data
            val stints = when (stintsResult) {
                is ApiResult.Success -> stintsResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> emptyList()
                is ApiResult.Loading -> emptyList()
            }
            
            // Combine data to create driver results
            val driverResults = drivers.mapNotNull { driver ->
                val position = positions.find { it.driverNumber == driver.driverNumber }?.position ?: return@mapNotNull null
                val bestLap = laps.filter { it.driverNumber == driver.driverNumber && it.lapDuration != null }
                    .minByOrNull { it.lapDuration!! }
                
                val lapTime = bestLap?.lapDuration?.formatLapTime() ?: "--:--"
                
                // Find the latest stint for this driver to get current tyre compound
                val currentStint = stints.filter { it.driverNumber == driver.driverNumber }
                    .maxByOrNull { it.stintNumber }
                val currentTyre = currentStint?.compound ?: "UNKNOWN"
                
                F1DriverResult(
                    driver = driver,
                    position = position,
                    lapTime = lapTime,
                    currentTyre = currentTyre
                )
            }.sortedBy { it.position }
            
            // Find fastest lap
            val fastestLap = driverResults.minByOrNull { result ->
                result.lapTime.replace(":", "").replace(".", "").toDoubleOrNull() ?: Double.MAX_VALUE
            }
            
            val raceData = F1RaceData(
                session = session,
                driverResults = driverResults,
                weather = weather,
                fastestLap = fastestLap
            )
            
            emit(ApiResult.Success(raceData))
            
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Error fetching race data"))
        }
    }
    
    override suspend fun getLatestRaceData(): Flow<ApiResult<F1RaceData>> = flow {
        emit(ApiResult.Loading)
        
        try {
            // First, get the latest race session
            val sessionResult = f1SessionApi.getLatestRaceSession()
            val session = when (sessionResult) {
                is ApiResult.Success -> sessionResult.data.toDomainModel()
                is ApiResult.Error -> {
                    emit(ApiResult.Error("Failed to get latest race session: ${sessionResult.message}"))
                    return@flow
                }
                is ApiResult.Loading -> {
                    emit(ApiResult.Loading)
                    return@flow
                }
            }
            
            // Now fetch all race data for this session
            val sessionKey = session.sessionKey
            val driversResult = f1DriverApi.getDriversForSession(sessionKey)
            val lapsResult = f1LapApi.getLaps(sessionKey)
            val sessionResultsResult = f1SessionApi.getSessionResults(sessionKey)
            val weatherResult = f1WeatherApi.getLatestWeather(sessionKey)
            val stintsResult = f1StintApi.getStints(sessionKey)
            
            // Process drivers data
            val drivers = when (driversResult) {
                is ApiResult.Success -> {
                    println("Drivers API response: ${driversResult.data.size} drivers")
                    driversResult.data.map { it.toDomainModel() }
                }
                is ApiResult.Error -> {
                    emit(ApiResult.Error("Failed to get drivers: ${driversResult.message}"))
                    return@flow
                }
                is ApiResult.Loading -> {
                    emit(ApiResult.Loading)
                    return@flow
                }
            }
            
            // Process laps data
            val laps = when (lapsResult) {
                is ApiResult.Success -> lapsResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> emptyList()
                is ApiResult.Loading -> emptyList()
            }
            
            // Process session results data (final race positions)
            val sessionResults = when (sessionResultsResult) {
                is ApiResult.Success -> {
                    println("Session results API response: ${sessionResultsResult.data.size} results")
                    sessionResultsResult.data.map { it.toDomainModel() }
                }
                is ApiResult.Error -> {
                    println("Session results API error: ${sessionResultsResult.message}")
                    emptyList()
                }
                is ApiResult.Loading -> emptyList()
            }
            
            // Fallback to positions if session results are empty
            val positionsResult = if (sessionResults.isEmpty()) {
                f1PositionApi.getPositions(sessionKey)
            } else null
            
            val fallbackPositions = if (sessionResults.isEmpty()) {
                println("Using fallback positions since session results are empty")
                when (positionsResult) {
                    is ApiResult.Success -> {
                        println("Fallback positions API response: ${positionsResult.data.size} positions")
                        positionsResult.data.map { it.toDomainModel() }
                    }
                    is ApiResult.Error -> {
                        println("Fallback positions API error: ${positionsResult.message}")
                        emptyList()
                    }
                    is ApiResult.Loading -> emptyList()
                    null -> emptyList()
                }
            } else emptyList()
            
            // Process weather data
            val weather = when (weatherResult) {
                is ApiResult.Success -> weatherResult.data.toDomainModel()
                is ApiResult.Error -> null
                is ApiResult.Loading -> null
            }
            
            // Process stint data
            val stints = when (stintsResult) {
                is ApiResult.Success -> stintsResult.data.map { it.toDomainModel() }
                is ApiResult.Error -> emptyList()
                is ApiResult.Loading -> emptyList()
            }
            
            // Combine data to create driver results
            val driverResults = drivers.mapNotNull { driver ->
                // Try session results first, fallback to live positions
                val position = if (sessionResults.isNotEmpty()) {
                    sessionResults.find { it.driverNumber == driver.driverNumber }?.position
                } else {
                    fallbackPositions.find { it.driverNumber == driver.driverNumber }?.position
                } ?: run {
                    println("No position found for driver ${driver.driverNumber}")
                    return@mapNotNull null
                }
                
                val bestLap = laps.filter { it.driverNumber == driver.driverNumber && it.lapDuration != null }
                    .minByOrNull { it.lapDuration!! }
                
                val lapTime = bestLap?.lapDuration?.formatLapTime() ?: "--:--"
                
                // Find the latest stint for this driver to get current tyre compound
                val currentStint = stints.filter { it.driverNumber == driver.driverNumber }
                    .maxByOrNull { it.stintNumber }
                val currentTyre = currentStint?.compound ?: "UNKNOWN"
                
                F1DriverResult(
                    driver = driver,
                    position = position,
                    lapTime = lapTime,
                    currentTyre = currentTyre
                )
            }.sortedBy { it.position }
            
            println("Final driver results count: ${driverResults.size}")
            
            // Find fastest lap
            val fastestLap = driverResults.minByOrNull { result ->
                result.lapTime.replace(":", "").replace(".", "").toDoubleOrNull() ?: Double.MAX_VALUE
            }
            
            val raceData = F1RaceData(
                session = session,
                driverResults = driverResults,
                weather = weather,
                fastestLap = fastestLap
            )
            
            emit(ApiResult.Success(raceData))
            
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Error fetching latest race data"))
        }
    }
    
    override suspend fun getWeather(sessionKey: Int): Flow<ApiResult<F1Weather>> = flow {
        emit(ApiResult.Loading)
        val result = f1WeatherApi.getLatestWeather(sessionKey)
        when (result) {
            is ApiResult.Success -> {
                val weather = result.data.toDomainModel()
                emit(ApiResult.Success(weather))
            }
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
}