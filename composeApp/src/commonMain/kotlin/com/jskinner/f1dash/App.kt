package com.jskinner.f1dash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jskinner.f1dash.data.api.ApiLogger
import com.jskinner.f1dash.di.modules.appModules
import com.jskinner.f1dash.presentation.navigation.F1BottomNavigation
import com.jskinner.f1dash.presentation.navigation.F1Destinations
import com.jskinner.f1dash.presentation.navigation.F1NavigationHandler
import com.jskinner.f1dash.presentation.screens.PreviousRacesScreen
import com.jskinner.f1dash.presentation.screens.F1ReplayScreen
import com.jskinner.f1dash.presentation.screens.RaceResultsScreen
import com.jskinner.f1dash.presentation.screens.StatsScreen
import com.jskinner.f1dash.presentation.screens.TeamsScreen
import com.jskinner.f1dash.presentation.theme.F1Theme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        F1Theme {
            LaunchedEffect(Unit) {
                ApiLogger.logApiConfiguration()
            }
            
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            val navigationHandler = remember {
                F1NavigationHandler(navController, snackbarHostState, scope)
            }
            
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
                            onShowToast = navigationHandler::showToast,
                            onNavigateToDriverDetail = navigationHandler::navigateToDriverDetail,
                            onNavigateToReplay = navigationHandler::navigateToReplay
                        )
                    }

                    composable(F1Destinations.PreviousRaces.route) {
                        PreviousRacesScreen(
                            onShowToast = navigationHandler::showToast,
                            onNavigateToRaceResults = { sessionKey ->
                                navigationHandler.showToast("Loading race results for session $sessionKey")
                                // TODO: Navigate to race results with specific session
                            },
                            onNavigateToReplay = { sessionKey ->
                                navController.navigate("${F1Destinations.Replay.route}/$sessionKey")
                            }
                        )
                    }
                    
                    composable(F1Destinations.Teams.route) {
                        TeamsScreen()
                    }
                    
                    composable(F1Destinations.Stats.route) {
                        StatsScreen()
                    }

                    composable(F1Destinations.Replay.route) {
                        F1ReplayScreen(
                            onShowToast = navigationHandler::showToast,
                            onNavigateToDriverDetail = navigationHandler::navigateToDriverDetail,
                            onNavigateBack = navigationHandler::navigateBack
                        )
                    }

                    composable("${F1Destinations.Replay.route}/{sessionKey}") { backStackEntry ->
                        val sessionKey = backStackEntry.arguments?.getString("sessionKey")?.toIntOrNull() ?: 0
                        F1ReplayScreen(
                            sessionKey = sessionKey,
                            onShowToast = navigationHandler::showToast,
                            onNavigateToDriverDetail = navigationHandler::navigateToDriverDetail,
                            onNavigateBack = navigationHandler::navigateBack
                        )
                    }
                }
            }
        }
    }
}