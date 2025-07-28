package com.jskinner.f1dash.presentation.viewmodels

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Session
import com.jskinner.f1dash.domain.repository.F1Repository
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container

sealed interface F1PreviousRacesState {
    data object Loading : F1PreviousRacesState
    data class Error(val isRefreshing: Boolean = false) : F1PreviousRacesState
    data class Content(
        val races: List<F1Session>,
        val selectedYear: Int = 2025,
        val isRefreshing: Boolean = false
    ) : F1PreviousRacesState
}

sealed class F1PreviousRacesSideEffect {
    data class ShowToast(val message: String) : F1PreviousRacesSideEffect()
    data class NavigateToRaceResults(val sessionKey: Int) : F1PreviousRacesSideEffect()
    data class NavigateToReplay(val sessionKey: Int) : F1PreviousRacesSideEffect()
    data object UnableToFetchError : F1PreviousRacesSideEffect()
    data object RefreshError : F1PreviousRacesSideEffect()
}

@OptIn(OrbitExperimental::class)
class F1PreviousRacesViewModel(
    private val f1Repository: F1Repository
) : BaseViewModel<F1PreviousRacesState, F1PreviousRacesSideEffect>() {

    override val container = container<F1PreviousRacesState, F1PreviousRacesSideEffect>(F1PreviousRacesState.Loading) {
        onLoad()
    }

    private fun onLoad() = intent {
        reduce { F1PreviousRacesState.Loading }
        loadRaces(2025)
    }

    fun loadRaces(year: Int, forceRefresh: Boolean = false) = intent {
        if (!forceRefresh) {
            reduce { F1PreviousRacesState.Loading }
        } else {
            reduce {
                when (val currentState = state) {
                    is F1PreviousRacesState.Content -> currentState.copy(isRefreshing = true)
                    else -> F1PreviousRacesState.Loading
                }
            }
        }

        runCatching {
            f1Repository.getSessions(year).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        // Keep current state during refresh
                    }
                    is ApiResult.Success -> {
                        val racesSessions = result.data.filter {
                            it.sessionName.contains("Race", ignoreCase = true) ||
                                    it.sessionType.contains("Race", ignoreCase = true)
                        }
                            .sortedByDescending { it.dateStart }

                        reduce {
                            F1PreviousRacesState.Content(
                                races = racesSessions,
                                selectedYear = year,
                                isRefreshing = false
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { F1PreviousRacesState.Error(isRefreshing = false) }
                        postSideEffect(F1PreviousRacesSideEffect.UnableToFetchError)
                    }
                }
            }
        }.onFailure { error ->
            handleError(error)
            reduce { F1PreviousRacesState.Error(isRefreshing = false) }
            postSideEffect(F1PreviousRacesSideEffect.UnableToFetchError)
        }
    }

    fun onRefresh() = intent {
        val currentYear = when (val currentState = state) {
            is F1PreviousRacesState.Content -> currentState.selectedYear
            else -> 2025
        }
        loadRaces(currentYear, forceRefresh = true)
    }

    fun onYearSelected(year: Int) = intent {
        loadRaces(year)
    }

    fun onRaceClick(sessionKey: Int) = intent {
        postSideEffect(F1PreviousRacesSideEffect.NavigateToRaceResults(sessionKey))
    }

    fun onReplayClick(sessionKey: Int) = intent {
        postSideEffect(F1PreviousRacesSideEffect.NavigateToReplay(sessionKey))
    }

    private fun handleError(error: Throwable) = intent {
        // Log error for debugging (implement logging as needed)
        // Consider global error handling for common scenarios
    }
}