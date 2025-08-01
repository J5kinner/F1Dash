package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.models.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class BaseRepository {

    protected suspend fun <T> handleApiCall(
        apiCall: suspend () -> ApiResult<T>
    ): Flow<ApiResult<T>> = flow {
        emit(ApiResult.Loading)
        try {
            val result = apiCall()
            emit(result)
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
        }
    }

    protected suspend fun <T, R> handleApiCallWithTransform(
        apiCall: suspend () -> ApiResult<T>,
        transform: suspend (T) -> R
    ): Flow<ApiResult<R>> = flow {
        emit(ApiResult.Loading)
        try {
            when (val result = apiCall()) {
                is ApiResult.Success -> {
                    val transformedData = transform(result.data)
                    emit(ApiResult.Success(transformedData))
                }

                is ApiResult.Error -> emit(ApiResult.Error(result.message))
                is ApiResult.Loading -> emit(ApiResult.Loading)
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 