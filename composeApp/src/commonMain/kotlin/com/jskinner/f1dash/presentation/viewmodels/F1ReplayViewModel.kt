package com.jskinner.f1dash.presentation.viewmodels

import com.jskinner.f1dash.domain.models.*
import com.jskinner.f1dash.domain.repository.F1Repository
import com.jskinner.f1dash.data.models.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

sealed interface F1ReplayScreenState {
    data object Loading : F1ReplayScreenState
    data class Ready(
        val raceReplay: F1RaceReplay,
        val replayState: F1ReplayState,
        val currentFrame: F1ReplayFrame?,
        val drivers: Map<Int, F1Driver>
    ) : F1ReplayScreenState

    data class Error(val message: String) : F1ReplayScreenState
}

sealed interface F1ReplaySideEffect {
    data class ShowToast(val message: String) : F1ReplaySideEffect
    data class NavigateToDriverDetail(val driverNumber: Int) : F1ReplaySideEffect
}

class F1ReplayViewModel(
    private val f1Repository: F1Repository
) : BaseViewModel<F1ReplayScreenState, F1ReplaySideEffect>(F1ReplayScreenState.Loading) {

    private var playbackJob: Job? = null
    private var currentSessionKey: Int = 0

    init {
        loadRaceReplay()
    }

    fun loadRaceReplay(sessionKey: Int = 0) = intent {
        currentSessionKey = sessionKey
        reduce { F1ReplayScreenState.Loading }

        if (sessionKey == 0) {
            reduce { F1ReplayScreenState.Error("Please select a race session to view replay") }
            return@intent
        }

        try {
            f1Repository.getRaceReplay(sessionKey)
                .collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            reduce { F1ReplayScreenState.Loading }
                        }

                        is ApiResult.Success -> {
                            val raceReplay = result.data
                            if (raceReplay.frames.isEmpty()) {
                                reduce { F1ReplayScreenState.Error("No replay data available for this session") }
                                return@collect
                            }

                            val replayState = F1ReplayState(
                                isPlaying = false,
                                currentFrameIndex = 0,
                                playbackSpeed = 1.0f,
                                totalFrames = raceReplay.frames.size
                            )

                            val currentFrame = raceReplay.frames.firstOrNull()

                            reduce {
                                F1ReplayScreenState.Ready(
                                    raceReplay = raceReplay,
                                    replayState = replayState,
                                    currentFrame = currentFrame,
                                    drivers = raceReplay.drivers
                                )
                            }
                        }

                        is ApiResult.Error -> {
                            reduce { F1ReplayScreenState.Error(result.message) }
                            postSideEffect(F1ReplaySideEffect.ShowToast("Failed to load race replay: ${result.message}"))
                        }
                    }
                }
        } catch (e: Exception) {
            reduce { F1ReplayScreenState.Error(e.message ?: "Unknown error occurred") }
            postSideEffect(F1ReplaySideEffect.ShowToast("Error loading race replay"))
        }
    }

    @Deprecated("Use real API data instead")
    private fun createMockRaceReplay() = intent {
        // Create a mock race replay for testing
        val mockSession = F1Session(
            sessionKey = 123456,
            meetingKey = 123,
            sessionName = "Belgian Grand Prix",
            sessionType = "Race",
            dateStart = "2024-07-28T15:00:00",
            dateEnd = "2024-07-28T17:00:00",
            location = "Spa-Francorchamps",
            countryName = "Belgium",
            countryCode = "BEL",
            circuitName = "Circuit de Spa-Francorchamps",
            circuitShortName = "Spa",
            year = 2024
        )

        val mockDrivers = createDriverMap()

        val mockFrames = (1..20).map { lapNumber ->
            F1ReplayFrame(
                lapNumber = lapNumber,
                elapsedTime = lapNumber * 120.0, // 2 minutes per lap
                driverPositions = mockDrivers.keys.take(10).mapIndexed { index, driverNumber ->
                    F1DriverPosition(
                        driverNumber = driverNumber,
                        position = index + 1,
                        gap = if (index == 0) "Leader" else "+${index * 2.5}s",
                        lapTime = "${1 + (index * 0.1)}:${30 + (index * 2)}.${100 + (index * 10)}",
                        tyre = listOf("SOFT", "MEDIUM", "HARD")[index % 3],
                        pitStops = index / 5
                    )
                }
            )
        }

        val mockRaceReplay = F1RaceReplay(
            session = mockSession,
            frames = mockFrames,
            totalLaps = 20,
            raceDuration = 2400.0, // 40 minutes
            drivers = mockDrivers
        )

        val replayState = F1ReplayState(
            isPlaying = false,
            currentFrameIndex = 0,
            playbackSpeed = 1.0f,
            totalFrames = mockFrames.size
        )

        val currentFrame = mockFrames.first()

        reduce {
            F1ReplayScreenState.Ready(
                raceReplay = mockRaceReplay,
                replayState = replayState,
                currentFrame = currentFrame,
                drivers = mockDrivers
            )
        }
    }

    private fun createDriverMap(): Map<Int, F1Driver> {
        val driverNames = mapOf(
            1 to "Max Verstappen",
            2 to "Logan Sargeant",
            3 to "Daniel Ricciardo",
            4 to "Lando Norris",
            5 to "Sebastian Vettel",
            6 to "Nicholas Latifi",
            7 to "Lewis Hamilton",
            8 to "Valtteri Bottas",
            9 to "Carlos Sainz",
            10 to "Pierre Gasly",
            11 to "Sergio Pérez",
            12 to "Esteban Ocon",
            14 to "Fernando Alonso",
            16 to "Charles Leclerc",
            18 to "Lance Stroll",
            20 to "Kevin Magnussen",
            22 to "Yuki Tsunoda",
            23 to "Alexander Albon",
            24 to "Zhou Guanyu",
            27 to "Nico Hulkenberg",
            31 to "Esteban Ocon",
            44 to "Lewis Hamilton",
            55 to "Carlos Sainz",
            63 to "George Russell",
            77 to "Valtteri Bottas",
            81 to "Oscar Piastri"
        )

        val teamNames = mapOf(
            1 to "Red Bull Racing",
            2 to "Williams",
            3 to "AlphaTauri",
            4 to "McLaren",
            5 to "Aston Martin",
            6 to "Williams",
            7 to "Mercedes",
            8 to "Alfa Romeo",
            9 to "Ferrari",
            10 to "Alpine",
            11 to "Red Bull Racing",
            12 to "Alpine",
            14 to "Aston Martin",
            16 to "Ferrari",
            18 to "Aston Martin",
            20 to "Haas F1 Team",
            22 to "AlphaTauri",
            23 to "Williams",
            24 to "Alfa Romeo",
            27 to "Haas F1 Team",
            31 to "Alpine",
            44 to "Mercedes",
            55 to "Ferrari",
            63 to "Mercedes",
            77 to "Alfa Romeo",
            81 to "McLaren"
        )

        val teamColors = mapOf(
            "Red Bull Racing" to "3671C6",
            "Williams" to "37BEDD",
            "AlphaTauri" to "5E8FAA",
            "McLaren" to "FF8700",
            "Aston Martin" to "229971",
            "Mercedes" to "6CD3BF",
            "Alfa Romeo" to "C92D4B",
            "Ferrari" to "F91536",
            "Alpine" to "229971",
            "Haas F1 Team" to "B6BAAB"
        )

        return (1..10).associateWith { driverNumber ->
            val fullName = driverNames[driverNumber] ?: "Driver $driverNumber"
            val teamName = teamNames[driverNumber] ?: "Unknown Team"
            val teamColor = teamColors[teamName] ?: "808080"

            F1Driver(
                driverNumber = driverNumber,
                fullName = fullName,
                firstName = fullName.split(" ").firstOrNull() ?: "Driver",
                lastName = fullName.split(" ").lastOrNull() ?: driverNumber.toString(),
                nameAcronym = fullName.split(" ").map { it.first() }.joinToString(""),
                teamName = teamName,
                teamColour = teamColor,
                headshotUrl = null,
                countryCode = "XX",
                broadcastName = fullName
            )
        }
    }

    fun togglePlayback() = intent {
        val currentState = container.stateFlow.value
        if (currentState is F1ReplayScreenState.Ready) {
            val newReplayState = currentState.replayState.copy(
                isPlaying = !currentState.replayState.isPlaying
            )

            reduce {
                currentState.copy(replayState = newReplayState)
            }

            if (newReplayState.isPlaying) {
                startPlayback()
            } else {
                stopPlayback()
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) = intent {
        val currentState = container.stateFlow.value
        if (currentState is F1ReplayScreenState.Ready) {
            val newReplayState = currentState.replayState.copy(playbackSpeed = speed)
            reduce { currentState.copy(replayState = newReplayState) }
        }
    }

    fun seekToFrame(frameIndex: Int) = intent {
        val currentState = container.stateFlow.value
        if (currentState is F1ReplayScreenState.Ready) {
            val clampedIndex = frameIndex.coerceIn(0, currentState.raceReplay.frames.size - 1)
            val newReplayState = currentState.replayState.copy(
                currentFrameIndex = clampedIndex,
                isPlaying = false
            )
            val newCurrentFrame = currentState.raceReplay.frames.getOrNull(clampedIndex)

            reduce {
                currentState.copy(
                    replayState = newReplayState,
                    currentFrame = newCurrentFrame
                )
            }

            stopPlayback()
        }
    }

    fun onDriverClick(driverNumber: Int) = intent {
        postSideEffect(F1ReplaySideEffect.NavigateToDriverDetail(driverNumber))
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                val currentState = container.stateFlow.value as? F1ReplayScreenState.Ready ?: break

                if (!currentState.replayState.isPlaying) break

                val nextFrameIndex = currentState.replayState.currentFrameIndex + 1
                if (nextFrameIndex >= currentState.raceReplay.frames.size) {
                    intent {
                        reduce {
                            currentState.copy(
                                replayState = currentState.replayState.copy(isPlaying = false)
                            )
                        }
                    }
                    break
                }

                val nextFrame = currentState.raceReplay.frames[nextFrameIndex]
                intent {
                    reduce {
                        currentState.copy(
                            replayState = currentState.replayState.copy(currentFrameIndex = nextFrameIndex),
                            currentFrame = nextFrame
                        )
                    }
                }

                delay((1000 / currentState.replayState.playbackSpeed).toLong())
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}