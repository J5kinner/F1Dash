package com.jskinner.f1dash

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import com.jskinner.f1dash.di.modules.appModules
import com.jskinner.f1dash.presentation.screens.DriversListScreen
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        MaterialTheme {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
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
                            // TODO: Navigate to driver detail screen
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Driver #$driverNumber selected",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onNavigateToDriverStandings = { sessionKey ->
                            // TODO: Navigate to driver standings screen
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "View standings for session $sessionKey",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}