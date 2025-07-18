package com.jskinner.f1dash.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.flow.catch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

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
) : ViewModel(), ContainerHost<F1DriversState, F1DriversSideEffect> {

    override val container: Container<F1DriversState, F1DriversSideEffect> =
        container(F1DriversState.Idle())

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
                        val currentQuery = getCurrentSearchQuery()
                        val filteredDrivers = filterDrivers(result.data, currentQuery)
                        reduce { 
                            F1DriversState.Success(
                                drivers = result.data,
                                searchQuery = currentQuery,
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

    fun loadDriversForSession(sessionKey: Int) = intent {
        reduce { 
            when (val currentState = state) {
                is F1DriversState.Loading -> currentState.copy(selectedSessionKey = sessionKey)
                is F1DriversState.Success -> currentState.copy(selectedSessionKey = sessionKey)
                is F1DriversState.Error -> currentState.copy(selectedSessionKey = sessionKey)
                is F1DriversState.Idle -> currentState.copy(selectedSessionKey = sessionKey)
            }
        }
        
        f1Repository.getDriversForSession(sessionKey)
            .catch { throwable ->
                reduce { 
                    F1DriversState.Error(
                        message = throwable.message ?: "Unknown error occurred",
                        drivers = getCurrentDrivers(),
                        searchQuery = getCurrentSearchQuery(),
                        filteredDrivers = getCurrentFilteredDrivers(),
                        selectedSessionKey = sessionKey
                    )
                }
                postSideEffect(F1DriversSideEffect.ShowToast(throwable.message ?: "Failed to load session drivers"))
            }
            .collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        reduce { 
                            F1DriversState.Loading(
                                drivers = getCurrentDrivers(),
                                searchQuery = getCurrentSearchQuery(),
                                filteredDrivers = getCurrentFilteredDrivers(),
                                selectedSessionKey = sessionKey
                            )
                        }
                    }
                    is ApiResult.Success -> {
                        val currentQuery = getCurrentSearchQuery()
                        val filteredDrivers = filterDrivers(result.data, currentQuery)
                        reduce { 
                            F1DriversState.Success(
                                drivers = result.data,
                                searchQuery = currentQuery,
                                filteredDrivers = filteredDrivers,
                                selectedSessionKey = sessionKey
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
                                selectedSessionKey = sessionKey
                            )
                        }
                        postSideEffect(F1DriversSideEffect.ShowToast(result.message))
                    }
                }
            }
    }

    fun onRefresh() = intent {
        val sessionKey = getCurrentSessionKey()
        if (sessionKey != null) {
            loadDriversForSession(sessionKey)
        } else {
            loadDrivers()
        }
    }

    fun onSearchQueryChanged(query: String) = intent {
        val drivers = getCurrentDrivers()
        val filteredDrivers = filterDrivers(drivers, query)
        reduce { 
            when (val currentState = state) {
                is F1DriversState.Loading -> currentState.copy(searchQuery = query, filteredDrivers = filteredDrivers)
                is F1DriversState.Success -> currentState.copy(searchQuery = query, filteredDrivers = filteredDrivers)
                is F1DriversState.Error -> currentState.copy(searchQuery = query, filteredDrivers = filteredDrivers)
                is F1DriversState.Idle -> currentState.copy(searchQuery = query, filteredDrivers = filteredDrivers)
            }
        }
    }

    fun onDriverClick(driverNumber: Int) = intent {
        postSideEffect(F1DriversSideEffect.NavigateToDriverDetail(driverNumber))
    }

    fun onViewStandingsClick() = intent {
        getCurrentSessionKey()?.let { sessionKey ->
            postSideEffect(F1DriversSideEffect.NavigateToDriverStandings(sessionKey))
        }
    }

    fun onClearError() = intent {
        reduce { 
            when (val currentState = state) {
                is F1DriversState.Error -> F1DriversState.Success(
                    drivers = currentState.drivers,
                    searchQuery = currentState.searchQuery,
                    filteredDrivers = currentState.filteredDrivers,
                    selectedSessionKey = currentState.selectedSessionKey
                )
                else -> currentState
            }
        }
    }

    fun onRetry() = intent {
        onRefresh()
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

    private fun filterDrivers(drivers: List<F1Driver>, query: String): List<F1Driver> {
        if (query.isBlank()) return drivers
        
        val lowerQuery = query.lowercase()
        return drivers.filter { driver ->
            driver.fullName.lowercase().contains(lowerQuery) ||
            driver.nameAcronym.lowercase().contains(lowerQuery) ||
            driver.teamName.lowercase().contains(lowerQuery) ||
            driver.driverNumber.toString().contains(query)
        }
    }
}