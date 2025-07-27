package com.jskinner.f1dash.presentation.viewmodels

import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.catch

sealed interface F1DriversState {
    data class Loading(
        val drivers: List<F1Driver> = emptyList(),
        val searchQuery: String = "",
        val filteredDrivers: List<F1Driver> = emptyList(),
        val selectedSessionKey: Int? = null
    ) : F1DriversState

    data class Success(
        val drivers: List<F1Driver>,
        val searchQuery: String = "",
        val filteredDrivers: List<F1Driver> = drivers,
        val selectedSessionKey: Int? = null
    ) : F1DriversState

    data class Error(
        val message: String,
        val drivers: List<F1Driver> = emptyList(),
        val searchQuery: String = "",
        val filteredDrivers: List<F1Driver> = emptyList(),
        val selectedSessionKey: Int? = null
    ) : F1DriversState

    data class Idle(
        val drivers: List<F1Driver> = emptyList(),
        val searchQuery: String = "",
        val filteredDrivers: List<F1Driver> = emptyList(),
        val selectedSessionKey: Int? = null
    ) : F1DriversState
}

sealed interface F1DriversSideEffect {
    data class ShowToast(val message: String) : F1DriversSideEffect
    data class NavigateToDriverDetail(val driverNumber: Int) : F1DriversSideEffect
    data class NavigateToDriverStandings(val sessionKey: Int) : F1DriversSideEffect
}

class F1DriversViewModel(
    private val f1Repository: F1Repository
) : BaseViewModel<F1DriversState, F1DriversSideEffect>(F1DriversState.Idle()) {

    init {
        loadDrivers()
    }

    fun loadDrivers() = intent {
        f1Repository.getDrivers()
            .catch { throwable ->
                reduce { 
                    F1DriversState.Error(
                        message = throwable.message ?: "Unknown error occurred",
                        drivers = getCurrentDrivers(),
                        searchQuery = getCurrentSearchQuery(),
                        filteredDrivers = getCurrentFilteredDrivers(),
                        selectedSessionKey = getCurrentSessionKey()
                    )
                }
                postSideEffect(F1DriversSideEffect.ShowToast(throwable.message ?: "Failed to load drivers"))
            }
            .collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        reduce { 
                            F1DriversState.Loading(
                                drivers = getCurrentDrivers(),
                                searchQuery = getCurrentSearchQuery(),
                                filteredDrivers = getCurrentFilteredDrivers(),
                                selectedSessionKey = getCurrentSessionKey()
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        val filteredDrivers = filterDrivers(result.data, getCurrentSearchQuery())
                        reduce { 
                            F1DriversState.Success(
                                drivers = result.data,
                                searchQuery = getCurrentSearchQuery(),
                                filteredDrivers = filteredDrivers,
                                selectedSessionKey = getCurrentSessionKey()
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { 
                            F1DriversState.Error(
                                message = result.message,
                                drivers = getCurrentDrivers(),
                                searchQuery = getCurrentSearchQuery(),
                                filteredDrivers = getCurrentFilteredDrivers(),
                                selectedSessionKey = getCurrentSessionKey()
                            )
                        }
                        postSideEffect(F1DriversSideEffect.ShowToast(result.message))
                    }
                }
            }
    }

    fun onRefresh() = intent {
        loadDrivers()
    }

    fun onDriverClick(driverNumber: Int) = intent {
        postSideEffect(F1DriversSideEffect.NavigateToDriverDetail(driverNumber))
    }

    fun onDriverStandingsClick(sessionKey: Int) = intent {
        postSideEffect(F1DriversSideEffect.NavigateToDriverStandings(sessionKey))
    }

    private fun filterDrivers(drivers: List<F1Driver>, query: String): List<F1Driver> {
        return if (query.isBlank()) {
            drivers
        } else {
            drivers.filter { driver ->
                driver.fullName.contains(query, ignoreCase = true) ||
                        driver.teamName.contains(query, ignoreCase = true) ||
                        driver.nameAcronym.contains(query, ignoreCase = true)
            }
        }
    }

    private fun getCurrentDrivers(): List<F1Driver> {
        return when (val currentState = container.stateFlow.value) {
            is F1DriversState.Loading -> currentState.drivers
            is F1DriversState.Success -> currentState.drivers
            is F1DriversState.Error -> currentState.drivers
            is F1DriversState.Idle -> currentState.drivers
        }
    }

    private fun getCurrentSearchQuery(): String {
        return when (val currentState = container.stateFlow.value) {
            is F1DriversState.Loading -> currentState.searchQuery
            is F1DriversState.Success -> currentState.searchQuery
            is F1DriversState.Error -> currentState.searchQuery
            is F1DriversState.Idle -> currentState.searchQuery
        }
    }

    private fun getCurrentFilteredDrivers(): List<F1Driver> {
        return when (val currentState = container.stateFlow.value) {
            is F1DriversState.Loading -> currentState.filteredDrivers
            is F1DriversState.Success -> currentState.filteredDrivers
            is F1DriversState.Error -> currentState.filteredDrivers
            is F1DriversState.Idle -> currentState.filteredDrivers
        }
    }

    private fun getCurrentSessionKey(): Int? {
        return when (val currentState = container.stateFlow.value) {
            is F1DriversState.Loading -> currentState.selectedSessionKey
            is F1DriversState.Success -> currentState.selectedSessionKey
            is F1DriversState.Error -> currentState.selectedSessionKey
            is F1DriversState.Idle -> currentState.selectedSessionKey
        }
    }
}