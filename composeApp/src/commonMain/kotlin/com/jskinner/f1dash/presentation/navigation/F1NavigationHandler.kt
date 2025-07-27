package com.jskinner.f1dash.presentation.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class F1NavigationHandler(
    private val navController: NavController,
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope
) {

    fun showToast(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun navigateToDriverDetail(driverNumber: Int) {
        showToast("Driver #$driverNumber selected")
    }

    fun navigateToReplay() {
        navController.navigate(F1Destinations.Replay.route)
    }

    fun navigateToReplayWithSession(sessionKey: Int) {
        showToast("Loading replay for session $sessionKey")
        navController.navigate(F1Destinations.Replay.route)
    }

    fun navigateBack() {
        navController.popBackStack()
    }
} 