package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1ApiClient
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class F1RepositoryImpl(
    private val f1ApiClient: F1ApiClient
) : F1Repository {
    
    override suspend fun getDrivers(): Flow<ApiResult<List<F1Driver>>> = flow {
        emit(ApiResult.Loading)
        val result = f1ApiClient.getDrivers()
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
        val result = f1ApiClient.getDriversForSession(sessionKey)
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
        val result = f1ApiClient.getDriverByNumber(driverNumber, sessionKey)
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
        val result = f1ApiClient.getSessions(year)
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
        val result = f1ApiClient.getLatestSession()
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
            val sessionResult = f1ApiClient.getLatestSession()
            val driversResult = f1ApiClient.getDriversForSession(sessionKey)
            val lapsResult = f1ApiClient.getLaps(sessionKey)
            val positionsResult = f1ApiClient.getPositions(sessionKey)
            val weatherResult = f1ApiClient.getLatestWeather(sessionKey)
            
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
            
            // Combine data to create driver results
            val driverResults = drivers.mapNotNull { driver ->
                val position = positions.find { it.driverNumber == driver.driverNumber }?.position ?: return@mapNotNull null
                val bestLap = laps.filter { it.driverNumber == driver.driverNumber && it.lapDuration != null }
                    .minByOrNull { it.lapDuration!! }
                
                val lapTime = bestLap?.lapDuration?.formatLapTime() ?: "--:--"
                
                F1DriverResult(
                    driver = driver,
                    position = position,
                    lapTime = lapTime
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
    
    override suspend fun getWeather(sessionKey: Int): Flow<ApiResult<F1Weather>> = flow {
        emit(ApiResult.Loading)
        val result = f1ApiClient.getLatestWeather(sessionKey)
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