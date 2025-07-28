package com.jskinner.f1dash.data.transformers

import com.jskinner.f1dash.data.api.OpenF1RaceReplayData
import com.jskinner.f1dash.data.models.*
import com.jskinner.f1dash.domain.models.*

object OpenF1DataTransformer {

    fun transformToRaceReplay(
        openF1Data: OpenF1RaceReplayData,
        session: F1Session,
        drivers: Map<Int, F1Driver>
    ): F1RaceReplay {
        val frames = buildReplayFrames(openF1Data, drivers)
        val totalLaps = openF1Data.laps.maxOfOrNull { it.lapNumber } ?: 0
        val raceDuration = calculateRaceDuration(openF1Data)

        return F1RaceReplay(
            session = session,
            frames = frames.sortedBy { it.lapNumber },
            totalLaps = totalLaps,
            raceDuration = raceDuration,
            drivers = drivers
        )
    }

    private fun buildReplayFrames(
        openF1Data: OpenF1RaceReplayData,
        drivers: Map<Int, F1Driver>
    ): List<F1ReplayFrame> {
        val frames = mutableListOf<F1ReplayFrame>()

        // Get all lap numbers from lap data (this is more reliable than positions)
        val lapNumbersFromLaps = openF1Data.laps.map { it.lapNumber }.distinct().sorted()

        if (lapNumbersFromLaps.isEmpty()) {
            // Fallback: create frames based on available data
            return listOf(createFallbackFrame(openF1Data, drivers))
        }

        for (lapNumber in lapNumbersFromLaps) {
            val frame = buildFrameForLap(lapNumber, openF1Data, drivers)
            if (frame.driverPositions.isNotEmpty()) {
                frames.add(frame)
            }
        }

        return frames
    }

    private fun createFallbackFrame(
        openF1Data: OpenF1RaceReplayData,
        drivers: Map<Int, F1Driver>
    ): F1ReplayFrame {
        // Create a single frame with all available data
        val driverNumbers = drivers.keys
        val driverPositions = driverNumbers.mapNotNull { driverNumber ->
            val mostRecentPosition = openF1Data.positions
                .filter { it.driverNumber == driverNumber }
                .maxByOrNull { it.date }

            val lapData = openF1Data.laps
                .filter { it.driverNumber == driverNumber }
                .maxByOrNull { it.lapNumber }

            if (mostRecentPosition != null) {
                F1DriverPosition(
                    driverNumber = driverNumber,
                    position = mostRecentPosition.position,
                    gap = "0.000",
                    lapTime = formatLapTime(lapData?.lapDuration),
                    tyre = "UNKNOWN",
                    pitStops = 0
                )
            } else null
        }.sortedBy { it.position }

        return F1ReplayFrame(
            lapNumber = 1,
            elapsedTime = 0.0,
            driverPositions = driverPositions
        )
    }

    private fun buildFrameForLap(
        lapNumber: Int,
        openF1Data: OpenF1RaceReplayData,
        drivers: Map<Int, F1Driver>
    ): F1ReplayFrame {
        val lapsAtThisNumber = openF1Data.laps.filter { it.lapNumber == lapNumber }
        val driverNumbers = getRelevantDriverNumbers(lapsAtThisNumber, drivers)
        val driverPositions = buildDriverPositions(driverNumbers, lapNumber, lapsAtThisNumber, openF1Data)
        val elapsedTime = calculateElapsedTime(lapNumber, openF1Data.laps)

        return F1ReplayFrame(
            lapNumber = lapNumber,
            elapsedTime = elapsedTime,
            driverPositions = driverPositions.sortedBy { it.position }
        )
    }

    private fun getRelevantDriverNumbers(
        lapsAtThisNumber: List<OpenF1LapResponse>,
        drivers: Map<Int, F1Driver>
    ): List<Int> {
        return if (lapsAtThisNumber.isNotEmpty()) {
            lapsAtThisNumber.map { it.driverNumber }.distinct()
        } else {
            drivers.keys.toList()
        }
    }

    private fun buildDriverPositions(
        driverNumbers: List<Int>,
        lapNumber: Int,
        lapsAtThisNumber: List<OpenF1LapResponse>,
        openF1Data: OpenF1RaceReplayData
    ): List<F1DriverPosition> {
        return driverNumbers.mapNotNull { driverNumber ->
            buildDriverPosition(driverNumber, lapNumber, lapsAtThisNumber, openF1Data)
        }
    }

    private fun buildDriverPosition(
        driverNumber: Int,
        lapNumber: Int,
        lapsAtThisNumber: List<OpenF1LapResponse>,
        openF1Data: OpenF1RaceReplayData
    ): F1DriverPosition? {
        val mostRecentPosition = findMostRecentPositionForLap(driverNumber, lapNumber, openF1Data.positions)
        val position = mostRecentPosition?.position ?: return null

        val lapData = lapsAtThisNumber.find { it.driverNumber == driverNumber }
        val intervalData = findMostRecentIntervalForLap(driverNumber, lapNumber, openF1Data.intervals)
        val currentStint = findCurrentStint(driverNumber, lapNumber, openF1Data.stints)
        val pitStops = countPitStopsUpToLap(driverNumber, lapNumber, openF1Data.pitStops)

        val gap = formatGap(intervalData?.gap, intervalData?.interval)
        val lapTime = getLapTimeForDriver(lapData, driverNumber, lapNumber, openF1Data.laps)
        val tyre = currentStint?.compound ?: "UNKNOWN"

        return F1DriverPosition(
            driverNumber = driverNumber,
            position = position,
            gap = gap,
            lapTime = lapTime,
            tyre = tyre,
            pitStops = pitStops
        )
    }

    private fun getLapTimeForDriver(
        lapData: OpenF1LapResponse?,
        driverNumber: Int,
        lapNumber: Int,
        allLaps: List<OpenF1LapResponse>
    ): String {
        return when {
            lapData?.lapDuration != null -> formatLapTime(lapData.lapDuration)
            else -> findMostRecentLapTime(driverNumber, lapNumber, allLaps)
        }
    }

    private fun findMostRecentPositionForLap(
        driverNumber: Int,
        targetLapNumber: Int,
        positions: List<OpenF1PositionResponse>
    ): OpenF1PositionResponse? {
        return positions
            .filter { it.driverNumber == driverNumber }
            .sortedByDescending { it.date }
            .firstOrNull()
    }

    private fun findMostRecentIntervalForLap(
        driverNumber: Int,
        targetLapNumber: Int,
        intervals: List<OpenF1IntervalResponse>
    ): OpenF1IntervalResponse? {
        return intervals
            .filter { it.driverNumber == driverNumber }
            .sortedByDescending { it.date }
            .firstOrNull()
    }

    private fun findCurrentStint(
        driverNumber: Int,
        lapNumber: Int,
        stints: List<OpenF1StintResponse>
    ): OpenF1StintResponse? {
        return stints
            .filter { it.driverNumber == driverNumber }
            .filter { it.lapStart != null && lapNumber >= it.lapStart }
            .filter { it.lapEnd == null || lapNumber <= it.lapEnd }
            .maxByOrNull { it.stintNumber }
    }

    private fun countPitStopsUpToLap(
        driverNumber: Int,
        lapNumber: Int,
        pitStops: List<OpenF1PitResponse>
    ): Int {
        return pitStops
            .filter { it.driverNumber == driverNumber }
            .count { it.lapNumber <= lapNumber }
    }

    private fun findMostRecentLapTime(
        driverNumber: Int,
        upToLapNumber: Int,
        laps: List<OpenF1LapResponse>
    ): String {
        val recentLap = laps
            .filter { it.driverNumber == driverNumber }
            .filter { it.lapNumber <= upToLapNumber }
            .filter { it.lapDuration != null && it.lapDuration > 0 }
            .maxByOrNull { it.lapNumber }

        return formatLapTime(recentLap?.lapDuration)
    }

    private fun formatGap(gap: String?, interval: String?): String {
        return when {
            gap == null && interval == null -> "+0.000"
            gap != null && gap == "0.0" -> "0.000"
            gap != null -> {
                if (gap.contains("LAP")) gap else "+$gap"
            }

            interval != null -> {
                if (interval.contains("LAP")) interval else "+$interval"
            }
            else -> "+0.000"
        }
    }

    private fun formatLapTime(lapDuration: Double?): String {
        if (lapDuration == null || lapDuration <= 0) return "--:--"

        val totalSeconds = lapDuration
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        val wholeSeconds = seconds.toInt()
        val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()

        return "${minutes}:${wholeSeconds.toString().padStart(2, '0')}.${milliseconds.toString().padStart(3, '0')}"
    }

    private fun calculateElapsedTime(lapNumber: Int, laps: List<OpenF1LapResponse>): Double {
        return laps
            .filter { it.lapNumber <= lapNumber }
            .mapNotNull { it.lapDuration }
            .sum()
    }

    private fun calculateRaceDuration(openF1Data: OpenF1RaceReplayData): Double {
        return openF1Data.laps.mapNotNull { it.lapDuration }.sum()
    }

    fun transformOpenF1PositionToDriverPosition(
        position: OpenF1PositionResponse,
        lapData: OpenF1LapResponse?,
        intervalData: OpenF1IntervalResponse?,
        stint: OpenF1StintResponse?,
        pitStopCount: Int
    ): F1DriverPosition {
        return F1DriverPosition(
            driverNumber = position.driverNumber,
            position = position.position,
            gap = formatGap(intervalData?.gap, intervalData?.interval),
            lapTime = formatLapTime(lapData?.lapDuration),
            tyre = stint?.compound ?: "UNKNOWN",
            pitStops = pitStopCount
        )
    }
}