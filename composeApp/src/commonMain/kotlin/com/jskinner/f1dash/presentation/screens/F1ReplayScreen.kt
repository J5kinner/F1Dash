package com.jskinner.f1dash.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.presentation.viewmodels.F1ReplayScreenState
import com.jskinner.f1dash.presentation.viewmodels.F1ReplaySideEffect
import com.jskinner.f1dash.presentation.viewmodels.F1ReplayViewModel
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun F1ReplayScreen(
    sessionKey: Int = 0,
    viewModel: F1ReplayViewModel = koinInject(),
    onShowToast: (String) -> Unit = {},
    onNavigateToDriverDetail: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is F1ReplaySideEffect.ShowToast -> onShowToast(sideEffect.message)
            is F1ReplaySideEffect.NavigateToDriverDetail -> onNavigateToDriverDetail(sideEffect.driverNumber)
        }
    }

    // Load race replay when sessionKey is provided
    LaunchedEffect(sessionKey) {
        if (sessionKey > 0) {
            viewModel.loadRaceReplay(sessionKey)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Race Replay") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            when (val currentState = state) {
                is F1ReplayScreenState.Ready -> {
                    ReplayControls(
                        replayState = currentState.replayState,
                        totalFrames = currentState.raceReplay.frames.size,
                        onTogglePlayback = viewModel::togglePlayback,
                        onSeekTo = viewModel::seekToFrame,
                        onSpeedChange = viewModel::setPlaybackSpeed
                    )
                }

                else -> Unit
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val currentState = state) {
            is F1ReplayScreenState.Loading -> {
                LoadingContent()
            }

            is F1ReplayScreenState.Error -> {
                ErrorContent(
                    error = currentState.message,
                    onRetry = { viewModel.loadRaceReplay(sessionKey) }
                )
            }

            is F1ReplayScreenState.Ready -> {
                ReplayContent(
                    raceReplay = currentState.raceReplay,
                    currentFrame = currentState.currentFrame,
                    drivers = currentState.drivers,
                    replayState = currentState.replayState,
                    onDriverClick = viewModel::onDriverClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ReplayControls(
    replayState: F1ReplayState,
    totalFrames: Int,
    onTogglePlayback: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress Slider
            Column {
                Slider(
                    value = replayState.currentFrameIndex.toFloat(),
                    onValueChange = { onSeekTo(it.toInt()) },
                    valueRange = 0f..(totalFrames - 1).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lap ${replayState.currentFrameIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: $totalFrames",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${replayState.playbackSpeed}x",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onSpeedChange((replayState.playbackSpeed - 0.5f).coerceAtLeast(0.5f)) }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Slower")
                    }

                    IconButton(
                        onClick = { onSpeedChange((replayState.playbackSpeed + 0.5f).coerceAtMost(4.0f)) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Faster")
                    }
                }

                // Play/Pause
                FilledIconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (replayState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (replayState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onSeekTo((replayState.currentFrameIndex - 10).coerceAtLeast(0)) }
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Skip Back")
                    }

                    IconButton(
                        onClick = { onSeekTo((replayState.currentFrameIndex + 10).coerceAtMost(totalFrames - 1)) }
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Skip Forward")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplayContent(
    raceReplay: F1RaceReplay,
    currentFrame: F1ReplayFrame?,
    drivers: Map<Int, F1Driver>,
    replayState: F1ReplayState,
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
            ReplayRaceHeader(
                session = raceReplay.session,
                currentFrame = currentFrame,
                isPlaying = replayState.isPlaying
            )
        }

        // Driver Positions
        currentFrame?.let { frame ->
            items(frame.driverPositions) { position ->
                val driver = drivers[position.driverNumber]
                if (driver != null) {
                    ReplayDriverCard(
                        driver = driver,
                        position = position,
                        onClick = { onDriverClick(position.driverNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplayRaceHeader(
    session: F1Session,
    currentFrame: F1ReplayFrame?,
    isPlaying: Boolean
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.circuitName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${session.location}, ${session.countryName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Live Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isPlaying) Color.Red else Color.Gray,
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Text(
                        text = if (isPlaying) "LIVE" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            currentFrame?.let { frame ->
                Spacer(modifier = Modifier.height(8.dp))
                val elapsedTimer = (frame.elapsedTime / 60).roundToInt()
                Text(
                    text = "Lap ${frame.lapNumber} • $elapsedTimer min",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ReplayDriverCard(
    driver: F1Driver,
    position: F1DriverPosition,
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
                        color = when (position.position) {
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
                    text = position.position.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Driver info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = driver.fullName,
                    style = MaterialTheme.typography.bodyLarge,
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
            }

            // Race info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = position.gap,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = position.lapTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tyre compound
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = getTyreCompoundColor(position.tyre),
                        shape = CircleShape,
                        modifier = Modifier.size(12.dp)
                    ) {}
                    Text(
                        text = position.tyre,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (position.pitStops > 0) {
                    Text(
                        text = "${position.pitStops} stops",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            Text("Loading replay data...")
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

private fun getTyreCompoundColor(compound: String): Color {
    return when (compound.uppercase()) {
        "SOFT" -> Color(0xFFFF3333) // Red
        "MEDIUM" -> Color(0xFFFFD700) // Yellow
        "HARD" -> Color(0xFFFFFFFF) // White
        "INTERMEDIATE" -> Color(0xFF00FF00) // Green
        "WET" -> Color(0xFF0080FF) // Blue
        else -> Color.Gray
    }
}