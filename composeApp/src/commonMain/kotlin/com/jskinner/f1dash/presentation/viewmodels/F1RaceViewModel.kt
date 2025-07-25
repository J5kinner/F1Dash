package com.jskinner.f1dash.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1RaceData
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

sealed interface F1RaceState {
    data class Loading(
        val raceData: F1RaceData? = null
    ) : F1RaceState

    data class Success(
        val raceData: F1RaceData
    ) : F1RaceState

    data class Error(
        val message: String,
        val raceData: F1RaceData? = null
    ) : F1RaceState

    data object Idle : F1RaceState
}

sealed interface F1RaceSideEffect {
    data class ShowToast(val message: String) : F1RaceSideEffect
    data class NavigateToDriverDetail(val driverNumber: Int) : F1RaceSideEffect
}

class F1RaceViewModel(
    private val f1Repository: F1Repository
) : ViewModel(), ContainerHost<F1RaceState, F1RaceSideEffect> {

    override val container: Container<F1RaceState, F1RaceSideEffect> =
        container(F1RaceState.Idle)

    init {
        loadRaceData()
    }

    fun loadRaceData() = intent {
        f1Repository.getLatestRaceData()
            .catch { throwable ->
                reduce { 
                    F1RaceState.Error(
                        message = throwable.message ?: "Unknown error occurred",
                        raceData = getCurrentRaceData()
                    )
                }
                postSideEffect(F1RaceSideEffect.ShowToast(throwable.message ?: "Failed to load race data"))
            }
            .collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        reduce { 
                            F1RaceState.Loading(
                                raceData = getCurrentRaceData()
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        reduce { 
                            F1RaceState.Success(
                                raceData = result.data
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { 
                            F1RaceState.Error(
                                message = result.message,
                                raceData = getCurrentRaceData()
                            )
                        }
                        postSideEffect(F1RaceSideEffect.ShowToast(result.message))
                    }
                }
            }
    }

    fun onRefresh() = intent {
        loadRaceData()
    }

    fun onDriverClick(driverNumber: Int) = intent {
        postSideEffect(F1RaceSideEffect.NavigateToDriverDetail(driverNumber))
    }

    private fun getCurrentRaceData(): F1RaceData? {
        return when (val currentState = container.stateFlow.value) {
            is F1RaceState.Loading -> currentState.raceData
            is F1RaceState.Success -> currentState.raceData
            is F1RaceState.Error -> currentState.raceData
            is F1RaceState.Idle -> null
        }
    }
}