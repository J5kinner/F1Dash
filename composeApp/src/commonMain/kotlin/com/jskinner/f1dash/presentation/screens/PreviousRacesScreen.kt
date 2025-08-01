package com.jskinner.f1dash.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jskinner.f1dash.domain.models.F1Session
import com.jskinner.f1dash.presentation.viewmodels.F1PreviousRacesSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1PreviousRacesState
import com.jskinner.f1dash.presentation.viewmodels.F1PreviousRacesViewModel
import f1dash.composeapp.generated.resources.Res
import f1dash.composeapp.generated.resources.screen_title_previous_races
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousRacesScreen(
    viewModel: F1PreviousRacesViewModel = koinInject(),
    onShowToast: (String) -> Unit = {},
    onNavigateToRaceResults: (Int) -> Unit = {},
    onNavigateToReplay: (Int) -> Unit = {}
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is F1PreviousRacesSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is F1PreviousRacesSideEffect.NavigateToRaceResults -> onNavigateToRaceResults(sideEffect.sessionKey)
            is F1PreviousRacesSideEffect.NavigateToReplay -> onNavigateToReplay(sideEffect.sessionKey)
            F1PreviousRacesSideEffect.UnableToFetchError -> onShowToast("Unable to fetch races")
            F1PreviousRacesSideEffect.RefreshError -> onShowToast("Failed to refresh races")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.screen_title_previous_races)) },
                actions = {
                    when (val currentState = state) {
                        is F1PreviousRacesState.Content -> {
                            YearSelector(
                                selectedYear = currentState.selectedYear,
                                onYearSelected = viewModel::onYearSelected
                            )
                        }

                        else -> {}
                    }
                    
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is F1PreviousRacesState.Loading -> {
                LoadingContent()
            }

            is F1PreviousRacesState.Content -> {
                PullRefreshContent(
                    isRefreshing = currentState.isRefreshing,
                    onRefresh = viewModel::onRefresh
                ) {
                    PreviousRacesContent(
                        races = currentState.races,
                        selectedYear = currentState.selectedYear,
                        onRaceClick = viewModel::onRaceClick,
                        onReplayClick = viewModel::onReplayClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

            is F1PreviousRacesState.Error -> {
                ErrorContent(
                    error = "Unable to load races",
                    onRetry = viewModel::onRefresh
                )
            }
        }
    }
}

@Composable
private fun PreviousRacesContent(
    races: List<F1Session>,
    selectedYear: Int,
    onRaceClick: (Int) -> Unit,
    onReplayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "$selectedYear F1 Season",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(races) { race ->
            RaceCard(
                race = race,
                onClick = { onRaceClick(race.sessionKey) },
                onReplayClick = { onReplayClick(race.sessionKey) }
            )
        }
    }
}

@Composable
private fun RaceCard(
    race: F1Session,
    onClick: () -> Unit,
    onReplayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with race name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = race.circuitName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${race.location}, ${race.countryName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status badge
                val isUpcoming = isUpcomingRace(race.dateStart)
                val statusColor = if (isUpcoming) Color(0xFFFF9800) else Color(0xFF4CAF50)
                val statusText = if (isUpcoming) "Upcoming" else "Completed"

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Race details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatRaceDate(race.dateStart),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = race.sessionName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isUpcomingRace(race.dateStart)) {
                        // Replay button for completed races
                        IconButton(
                            onClick = onReplayClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Watch Replay",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Results button
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = "View Results",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
            Text("Loading races...")
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

private fun formatRaceDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val day = localDateTime.dayOfMonth
        val year = localDateTime.year
        "$month $day, $year"
    } catch (e: Exception) {
        "Unknown Date"
    }
}

private fun isUpcomingRace(dateString: String): Boolean {
    return try {
        val raceInstant = Instant.parse(dateString)
        val currentInstant = Clock.System.now()
        raceInstant > currentInstant
    } catch (e: Exception) {
        false
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSelector(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableYears = listOf(2025, 2024)

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.padding(end = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Select year",
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = year.toString(),
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    },
                    leadingIcon = if (year == selectedYear) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null
                )
            }
        }
    }
} 