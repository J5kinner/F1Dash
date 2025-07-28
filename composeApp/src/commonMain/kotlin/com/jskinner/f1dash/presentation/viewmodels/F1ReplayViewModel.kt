package com.jskinner.f1dash.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.jskinner.f1dash.data.models.ApiResult
import com.jskinner.f1dash.domain.models.F1Driver
import com.jskinner.f1dash.domain.models.F1RaceReplay
import com.jskinner.f1dash.domain.models.F1ReplayFrame
import com.jskinner.f1dash.domain.models.F1ReplayState
import com.jskinner.f1dash.domain.repository.F1Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.viewmodel.container

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
) : BaseViewModel<F1ReplayScreenState, F1ReplaySideEffect>() {

    override val container = container<F1ReplayScreenState, F1ReplaySideEffect>(F1ReplayScreenState.Loading)

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
        stopPlayback()
        playbackJob = viewModelScope.launch {
            while (true) {
                val currentState = container.stateFlow.value as? F1ReplayScreenState.Ready
                if (currentState == null || !currentState.replayState.isPlaying) {
                    break
                }

                delay((1000 / currentState.replayState.playbackSpeed).toLong())

                intent {
                    val latestState = container.stateFlow.value as? F1ReplayScreenState.Ready ?: return@intent

                    if (!latestState.replayState.isPlaying) {
                        return@intent
                    }

                    val nextFrameIndex = latestState.replayState.currentFrameIndex + 1
                    if (nextFrameIndex >= latestState.raceReplay.frames.size) {
                        reduce {
                            latestState.copy(
                                replayState = latestState.replayState.copy(isPlaying = false)
                            )
                        }
                        return@intent
                    }

                    val nextFrame = latestState.raceReplay.frames[nextFrameIndex]
                    
                    reduce {
                        latestState.copy(
                            replayState = latestState.replayState.copy(currentFrameIndex = nextFrameIndex),
                            currentFrame = nextFrame
                        )
                    }
                }
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