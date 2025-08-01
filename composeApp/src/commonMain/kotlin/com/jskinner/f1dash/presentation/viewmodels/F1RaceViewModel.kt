package com.jskinner.f1dash.presentation.viewmodels

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1RaceData
import com.jskinner.f1dash.domain.repository.F1Repository
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container

sealed interface F1RaceState {
    data object Loading : F1RaceState
    data class Error(val isRefreshing: Boolean = false) : F1RaceState
    data class Content(
        val raceData: F1RaceData,
        val isRefreshing: Boolean = false
    ) : F1RaceState
}

sealed class F1RaceSideEffect {
    data class ShowToast(val message: String) : F1RaceSideEffect()
    data class NavigateToDriverDetail(val driverNumber: Int) : F1RaceSideEffect()
    data class NavigateToReplay(val sessionKey: Int) : F1RaceSideEffect()
    data object UnableToFetchError : F1RaceSideEffect()
    data object RefreshError : F1RaceSideEffect()
}

@OptIn(OrbitExperimental::class)
class F1RaceViewModel(
    private val f1Repository: F1Repository
) : BaseViewModel<F1RaceState, F1RaceSideEffect>() {

    override val container = container<F1RaceState, F1RaceSideEffect>(F1RaceState.Loading) {
        onLoad()
    }

    private fun onLoad() = intent {
        reduce { F1RaceState.Loading }
        loadRaceData()
    }

    fun loadRaceData(forceRefresh: Boolean = false) = intent {
        if (!forceRefresh) {
            reduce { F1RaceState.Loading }
        } else {
            reduce {
                when (val currentState = state) {
                    is F1RaceState.Content -> currentState.copy(isRefreshing = true)
                    else -> F1RaceState.Loading
                }
            }
        }

        runCatching {
            f1Repository.getLatestRaceData(forceRefresh).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        // Keep current state during refresh
                    }
                    is ApiResult.Success -> {
                        reduce {
                            F1RaceState.Content(
                                raceData = result.data,
                                isRefreshing = false
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { F1RaceState.Error(isRefreshing = false) }
                        postSideEffect(F1RaceSideEffect.UnableToFetchError)
                    }
                }
            }
        }.onFailure { error ->
            handleError(error)
            reduce { F1RaceState.Error(isRefreshing = false) }
            postSideEffect(F1RaceSideEffect.UnableToFetchError)
        }
    }

    fun onRefresh() = intent {
        loadRaceData(forceRefresh = true)
    }

    fun onDriverClick(driverNumber: Int) = intent {
        postSideEffect(F1RaceSideEffect.NavigateToDriverDetail(driverNumber))
    }

    fun onReplayClick() = intent {
        runCatching {
            f1Repository.getLatestRaceSession().collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        postSideEffect(F1RaceSideEffect.NavigateToReplay(result.data.sessionKey))
                    }

                    is ApiResult.Error -> {
                        postSideEffect(F1RaceSideEffect.ShowToast("Unable to load latest race session"))
                    }

                    is ApiResult.Loading -> {}
                }
            }
        }.onFailure {
            postSideEffect(F1RaceSideEffect.ShowToast("Error loading latest race session"))
        }
    }

    private fun handleError(error: Throwable) = intent {
        // Log error for debugging (implement logging as needed)
        // Consider global error handling for common scenarios
    }
}