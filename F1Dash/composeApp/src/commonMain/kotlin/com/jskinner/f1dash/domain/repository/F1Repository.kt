package com.jskinner.f1dash.domain.repository

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import kotlinx.coroutines.flow.Flow

interface F1Repository {
    suspend fun getDrivers(): Flow<ApiResult<List<F1Driver>>>
    suspend fun getDriversForSession(sessionKey: Int): Flow<ApiResult<List<F1Driver>>>
    suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int? = null): Flow<ApiResult<F1Driver>>
}