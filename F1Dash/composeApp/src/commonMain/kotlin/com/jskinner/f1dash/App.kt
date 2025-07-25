package com.jskinner.f1dash

import androidx.compose.material3.MaterialTheme
import com.jskinner.f1dash.presentation.theme.F1Theme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import com.jskinner.f1dash.di.modules.appModules
import com.jskinner.f1dash.presentation.screens.DriversListScreen
import com.jskinner.f1dash.presentation.screens.RaceResultsScreen
import com.jskinner.f1dash.presentation.screens.TeamsScreen
import com.jskinner.f1dash.presentation.screens.StatsScreen
import com.jskinner.f1dash.presentation.navigation.F1BottomNavigation
import com.jskinner.f1dash.presentation.navigation.F1Destinations
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        F1Theme {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    F1BottomNavigation(navController = navController)
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = F1Destinations.Race.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(F1Destinations.Race.route) {
                        RaceResultsScreen(
                            onShowToast = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onNavigateToDriverDetail = { driverNumber ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Driver #$driverNumber selected",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                    
                    composable(F1Destinations.Drivers.route) {
                        DriversListScreen(
                            onShowToast = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onNavigateToDriverDetail = { driverNumber ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Driver #$driverNumber selected",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onNavigateToDriverStandings = { sessionKey ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "View standings for session $sessionKey",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                    
                    composable(F1Destinations.Teams.route) {
                        TeamsScreen()
                    }
                    
                    composable(F1Destinations.Stats.route) {
                        StatsScreen()
                    }
                }
            }
        }
    }
}