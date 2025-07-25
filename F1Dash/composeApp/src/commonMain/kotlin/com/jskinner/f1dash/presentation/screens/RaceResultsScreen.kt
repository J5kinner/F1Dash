package com.jskinner.f1dash.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1RaceViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1RaceSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1RaceState
import com.jskinner.f1dash.domain.models.F1DriverResult
import com.jskinner.f1dash.domain.models.F1RaceData
import com.jskinner.f1dash.domain.models.F1Weather

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceResultsScreen(
    viewModel: F1RaceViewModel = koinInject(),
    onShowToast: (String) -> Unit = {},
    onNavigateToDriverDetail: (Int) -> Unit = {}
) {
    val state by viewModel.collectAsState()
    
    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is F1RaceSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is F1RaceSideEffect.NavigateToDriverDetail -> onNavigateToDriverDetail(sideEffect.driverNumber)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Race") },
                actions = {
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
                if (currentState.raceData == null) {
                    LoadingContent()
                } else {
                    RaceContent(
                        raceData = currentState.raceData,
                        onDriverClick = viewModel::onDriverClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            
            is F1RaceState.Error -> {
                if (currentState.raceData == null) {
                    ErrorContent(
                        error = currentState.message,
                        onRetry = viewModel::onRefresh
                    )
                } else {
                    RaceContent(
                        raceData = currentState.raceData,
                        onDriverClick = viewModel::onDriverClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            
            is F1RaceState.Success -> {
                RaceContent(
                    raceData = currentState.raceData,
                    onDriverClick = viewModel::onDriverClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is F1RaceState.Idle -> {
                LoadingContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading race data...")
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) {
                    Text("Retry")
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
        
        // Weather
        raceData.weather?.let { weather ->
            item {
                WeatherCard(weather = weather)
            }
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
private fun DriverResultCard(
    result: F1DriverResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (result.position) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.position.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Driver headshot
            SubcomposeAsyncImage(
                model = result.driver.headshotUrl,
                contentDescription = "${result.driver.fullName} headshot",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default avatar",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Driver info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.driver.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = result.driver.teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Lap time
            Text(
                text = result.lapTime,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
private fun WeatherCard(
    weather: F1Weather
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weather",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Air Temperature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${weather.airTemperature.toInt()}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Track Temperature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${weather.trackTemperature.toInt()}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Humidity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${weather.humidity}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun parseTeamColor(teamColour: String): Color {
    return try {
        val colorString = if (teamColour.startsWith("#")) teamColour else "#$teamColour"
        val colorInt = colorString.removePrefix("#").toLong(16)
        Color(colorInt)
    } catch (_: Exception) {
        Color.Gray
    }
}