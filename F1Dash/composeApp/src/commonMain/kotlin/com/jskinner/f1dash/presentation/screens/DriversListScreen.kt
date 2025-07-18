package com.jskinner.f1dash.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1DriversViewModel
import com.jskinner.f1dash.presentation.viewmodels.F1DriversSideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1DriversState
import com.jskinner.f1dash.domain.models.F1Driver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversListScreen(
    viewModel: F1DriversViewModel = koinInject(),
    onShowToast: (String) -> Unit = {},
    onNavigateToDriverDetail: (Int) -> Unit = {},
    onNavigateToDriverStandings: (Int) -> Unit = {}
) {
    val state by viewModel.collectAsState()
    
    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is F1DriversSideEffect.ShowToast -> onShowToast(sideEffect.message)
            is F1DriversSideEffect.NavigateToDriverDetail -> onNavigateToDriverDetail(sideEffect.driverNumber)
            is F1DriversSideEffect.NavigateToDriverStandings -> onNavigateToDriverStandings(sideEffect.sessionKey)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("F1 Drivers") },
                actions = {
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = when (val currentState = state) {
                    is F1DriversState.Loading -> currentState.searchQuery
                    is F1DriversState.Success -> currentState.searchQuery
                    is F1DriversState.Error -> currentState.searchQuery
                    is F1DriversState.Idle -> currentState.searchQuery
                },
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
                
            // Content
            when (val currentState = state) {
                is F1DriversState.Loading -> {
                    if (currentState.drivers.isEmpty()) {
                        LoadingContent()
                    } else {
                        DriversContent(
                            drivers = currentState.filteredDrivers,
                            onDriverClick = viewModel::onDriverClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                is F1DriversState.Error -> {
                    if (currentState.drivers.isEmpty()) {
                        ErrorContent(
                            error = currentState.message,
                            onRetry = viewModel::onRetry,
                            onClearError = viewModel::onClearError
                        )
                    } else {
                        DriversContent(
                            drivers = currentState.filteredDrivers,
                            onDriverClick = viewModel::onDriverClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                is F1DriversState.Success -> {
                    if (currentState.filteredDrivers.isEmpty() && currentState.searchQuery.isNotBlank()) {
                        EmptySearchContent()
                    } else {
                        DriversContent(
                            drivers = currentState.filteredDrivers,
                            onDriverClick = viewModel::onDriverClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                is F1DriversState.Idle -> {
                    LoadingContent()
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search drivers...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
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
            Text("Loading drivers...")
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                    TextButton(onClick = onClearError) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "No results",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                "No drivers found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "Try adjusting your search query",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun DriversContent(
    drivers: List<F1Driver>,
    onDriverClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(drivers) { driver ->
            DriverCard(
                driver = driver,
                onClick = { onDriverClick(driver.driverNumber) }
            )
        }
    }
}

@Composable
private fun DriverCard(
    driver: F1Driver,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Driver headshot or placeholder
            SubcomposeAsyncImage(
                model = driver.headshotUrl,
                contentDescription = "${driver.fullName} headshot",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default avatar",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.outline
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
                    text = driver.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = driver.teamName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Driver number badge
                    Surface(
                        color = parseTeamColor(driver.teamColour),
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = driver.driverNumber.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Driver acronym
                    Text(
                        text = driver.nameAcronym,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Country code
                    Text(
                        text = driver.countryCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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