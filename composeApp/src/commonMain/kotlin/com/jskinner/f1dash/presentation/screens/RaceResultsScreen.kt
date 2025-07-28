package com.jskinner.f1dash.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jskinner.f1dash.domain.models.F1RaceData
import com.jskinner.f1dash.presentation.components.DriverResultCard
import com.jskinner.f1dash.presentation.components.ErrorContent
import com.jskinner.f1dash.presentation.components.LoadingContent
import com.jskinner.f1dash.presentation.viewmodels.F1RaceSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1RaceState
import com.jskinner.f1dash.presentation.viewmodels.F1RaceViewModel
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceResultsScreen(
    viewModel: F1RaceViewModel = koinInject(),
    onShowToast: (String) -> Unit = {},
    onNavigateToDriverDetail: (Int) -> Unit = {},
    onNavigateToReplay: () -> Unit = {}
) {
    val state by viewModel.collectAsState()
    
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is F1RaceSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is F1RaceSideEffect.NavigateToDriverDetail -> onNavigateToDriverDetail(sideEffect.driverNumber)
            F1RaceSideEffect.UnableToFetchError -> onShowToast("Unable to fetch race data")
            F1RaceSideEffect.RefreshError -> onShowToast("Failed to refresh data")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Race") },
                actions = {
                    IconButton(onClick = onNavigateToReplay) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Race Replay")
                    }
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val currentState = state) {
            is F1RaceState.Loading -> {
                LoadingContent()
            }
            
            is F1RaceState.Error -> {
                ErrorContent(
                    error = "Unable to load race data",
                    onRetry = viewModel::onRefresh
                )
            }

            is F1RaceState.Content -> {
                PullRefreshContent(
                    isRefreshing = currentState.isRefreshing,
                    onRefresh = viewModel::onRefresh
                ) {
                    RaceContent(
                        raceData = currentState.raceData,
                        onDriverClick = viewModel::onDriverClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun RaceContent(
    raceData: F1RaceData,
    onDriverClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Race Header
        item {
            RaceHeader(
                raceData = raceData
            )
        }
        
        // Driver Results
        items(raceData.driverResults) { result ->
            DriverResultCard(
                result = result,
                onClick = { onDriverClick(result.driver.driverNumber) }
            )
        }
        
        // Race Statistics
        item {
            RaceStatistics(
                raceData = raceData
            )
        }
    }
}

@Composable
private fun RaceHeader(
    raceData: F1RaceData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = raceData.session.circuitName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = raceData.session.sessionName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${raceData.session.location}, ${raceData.session.countryName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RaceStatistics(
    raceData: F1RaceData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Race Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            raceData.fastestLap?.let { fastestLap ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fastest Lap",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${fastestLap.lapTime} (${fastestLap.driver.nameAcronym})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Safety Car",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Out",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PullRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        content()
    }
}