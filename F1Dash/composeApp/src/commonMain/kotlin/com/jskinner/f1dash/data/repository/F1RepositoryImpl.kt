package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.api.F1ApiClient
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.models.toDomainModel
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
}