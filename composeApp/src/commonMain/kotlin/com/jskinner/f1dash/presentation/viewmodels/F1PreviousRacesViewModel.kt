package com.jskinner.f1dash.presentation.viewmodels

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Session
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.catch

sealed interface F1PreviousRacesState {
    data class Loading(
        val races: List<F1Session> = emptyList()
    ) : F1PreviousRacesState

    data class Success(
        val races: List<F1Session>
    ) : F1PreviousRacesState

    data class Error(
        val message: String,
        val races: List<F1Session> = emptyList()
    ) : F1PreviousRacesState

    data class Idle(
        val races: List<F1Session> = emptyList()
    ) : F1PreviousRacesState
}

sealed interface F1PreviousRacesSideEffect {
    data class ShowToast(val message: String) : F1PreviousRacesSideEffect
    data class NavigateToRaceResults(val sessionKey: Int) : F1PreviousRacesSideEffect
    data class NavigateToReplay(val sessionKey: Int) : F1PreviousRacesSideEffect
}

class F1PreviousRacesViewModel(
    private val f1Repository: F1Repository
) : BaseViewModel<F1PreviousRacesState, F1PreviousRacesSideEffect>(F1PreviousRacesState.Idle()) {

    init {
        loadRaces()
    }

    fun loadRaces() = intent {
        f1Repository.getSessions(year = 2025)
            .catch { throwable ->
                reduce {
                    F1PreviousRacesState.Error(
                        message = throwable.message ?: "Unknown error occurred",
                        races = getCurrentRaces()
                    )
                }
                postSideEffect(F1PreviousRacesSideEffect.ShowToast(throwable.message ?: "Failed to load races"))
            }
            .collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        reduce {
                            F1PreviousRacesState.Loading(
                                races = getCurrentRaces()
                            )
                        }
                    }

                    is ApiResult.Success -> {
                        // Debug: Log the raw data to see what we're getting
                        println("DEBUG: Raw sessions data count: ${result.data.size}")
                        result.data.take(5).forEach { session ->
                            println("DEBUG: Session - circuitName: '${session.circuitName}', location: '${session.location}', countryName: '${session.countryName}', sessionType: '${session.sessionType}', meetingKey: ${session.meetingKey}")
                        }

                        // Filter for race sessions, remove duplicates by meeting key, and sort by date
                        val raceSessions = result.data
                            .filter { it.sessionType == "Race" }
                            .distinctBy { it.meetingKey } // Remove duplicates by meeting key
                            .sortedBy { it.dateStart }

                        println("DEBUG: Filtered race sessions count: ${raceSessions.size}")
                        raceSessions.take(5).forEach { session ->
                            println("DEBUG: Race - circuitName: '${session.circuitName}', location: '${session.location}', countryName: '${session.countryName}', dateStart: '${session.dateStart}', meetingKey: ${session.meetingKey}")
                        }

                        reduce {
                            F1PreviousRacesState.Success(races = raceSessions)
                        }
                    }

                    is ApiResult.Error -> {
                        reduce {
                            F1PreviousRacesState.Error(
                                message = result.message,
                                races = getCurrentRaces()
                            )
                        }
                        postSideEffect(F1PreviousRacesSideEffect.ShowToast(result.message))
                    }
                }
            }
    }

    fun onRefresh() = intent {
        loadRaces()
    }

    fun onRaceClick(race: F1Session) = intent {
        postSideEffect(F1PreviousRacesSideEffect.NavigateToRaceResults(race.sessionKey))
    }

    fun onReplayClick(race: F1Session) = intent {
        postSideEffect(F1PreviousRacesSideEffect.NavigateToReplay(race.sessionKey))
    }

    private fun getCurrentRaces(): List<F1Session> {
        return when (val currentState = container.stateFlow.value) {
            is F1PreviousRacesState.Loading -> currentState.races
            is F1PreviousRacesState.Success -> currentState.races
            is F1PreviousRacesState.Error -> currentState.races
            is F1PreviousRacesState.Idle -> currentState.races
        }
    }
} 