package com.jskinner.f1dash.data.repository

import com.jskinner.f1dash.data.models.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class BaseRepository {

    protected fun <T> handleApiCall(
        apiCall: suspend () -> ApiResult<T>,
        transform: (T) -> T = { it }
    ): Flow<ApiResult<T>> = flow {
        emit(ApiResult.Loading)
        val result = apiCall()
        when (result) {
            is ApiResult.Success -> emit(ApiResult.Success(transform(result.data)))
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }

    protected fun <T, R> handleApiCallWithTransform(
        apiCall: suspend () -> ApiResult<T>,
        transform: (T) -> R
    ): Flow<ApiResult<R>> = flow {
        emit(ApiResult.Loading)
        val result = apiCall()
        when (result) {
            is ApiResult.Success -> emit(ApiResult.Success(transform(result.data)))
            is ApiResult.Error -> emit(ApiResult.Error(result.message))
            is ApiResult.Loading -> emit(ApiResult.Loading)
        }
    }
} 