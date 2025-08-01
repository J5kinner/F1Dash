package com.jskinner.f1dash.domain.repository

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.models.F1RaceData
import com.jskinner.f1dash.domain.models.F1RaceReplay
import com.jskinner.f1dash.domain.models.F1Session
import kotlinx.coroutines.flow.Flow

interface F1Repository {
    suspend fun getDrivers(): Flow<ApiResult<List<F1Driver>>>
    suspend fun getDriversForSession(sessionKey: Int): Flow<ApiResult<List<F1Driver>>>
    suspend fun getDriverByNumber(driverNumber: Int, sessionKey: Int? = null): Flow<ApiResult<F1Driver>>
    suspend fun getSessions(year: Int? = null): Flow<ApiResult<List<F1Session>>>
    suspend fun getLatestSession(): Flow<ApiResult<F1Session>>
    suspend fun getLatestRaceSession(): Flow<ApiResult<F1Session>>
    suspend fun getRaceData(sessionKey: Int): Flow<ApiResult<F1RaceData>>
    suspend fun getLatestRaceData(forceRefresh: Boolean = false): Flow<ApiResult<F1RaceData>>

    suspend fun getRaceReplay(sessionKey: Int): Flow<ApiResult<F1RaceReplay>>
}