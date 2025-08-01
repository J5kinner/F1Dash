package com.jskinner.f1dash.data.transformers

import com.jskinner.f1dash.data.api.OpenF1RaceReplayData
import com.jskinner.f1dash.data.models.OpenF1IntervalResponse
import com.jskinner.f1dash.data.models.OpenF1LapResponse
import com.jskinner.f1dash.data.models.OpenF1PitResponse
import com.jskinner.f1dash.data.models.OpenF1StintResponse
import com.jskinner.f1dash.domain.models.*

object OpenF1DataTransformer {

    fun transformToRaceReplay(
        session: F1Session,
        drivers: Map<Int, F1Driver>,
        openF1Data: OpenF1RaceReplayData
    ): F1RaceReplay {
        println("\n=== OPENF1 DATA TRANSFORMER ===")
        println("Processing session: ${session.circuitName} (${session.sessionKey})")
        println("Available drivers: ${drivers.keys.sorted().joinToString()}")
        println("OpenF1 Data:")
//        println("  - Positions: ${openF1Data.positions.size} entries")
        println("  - Laps: ${openF1Data.laps.size} entries")
        println("  - Intervals: ${openF1Data.intervals.size} entries")
        println("  - Stints: ${openF1Data.stints.size} entries")
        println("  - Pit stops: ${openF1Data.pitStops.size} entries")

        if (openF1Data.laps.isNotEmpty()) {
            println("\nSample lap data:")
            openF1Data.laps.take(5).forEach { lap ->
                println("  Driver ${lap.driverNumber}, Lap ${lap.lapNumber}: ${lap.lapDuration}s")
            }
        }

        if (openF1Data.stints.isNotEmpty()) {
            println("\nSample stint data:")
            openF1Data.stints.take(5).forEach { stint ->
                println("  Driver ${stint.driverNumber}, Stint ${stint.stintNumber}: ${stint.compound} (Laps ${stint.lapStart}-${stint.lapEnd})")
            }
        }

        val frames = createReplayFrames(openF1Data, drivers)

        val finalFrames = frames.ifEmpty {
            println("No OpenF1 frames created, using fallback data with available stint information")
            createFallbackFramesWithStints(drivers, openF1Data.stints, openF1Data.pitStops)
        }

        val totalLaps = finalFrames.maxOfOrNull { it.lapNumber } ?: 50
        val raceDuration = calculateRaceDuration(finalFrames)

        println("Final result: ${finalFrames.size} frames, ${totalLaps} laps")
        println("===============================\n")

        return F1RaceReplay(
            session = session,
            frames = finalFrames,
            totalLaps = totalLaps,
            raceDuration = raceDuration,
            drivers = drivers
        )
    }

    private fun createReplayFrames(
        openF1Data: OpenF1RaceReplayData,
        drivers: Map<Int, F1Driver>
    ): List<F1ReplayFrame> {
        val lapsByDriver = openF1Data.laps
            .groupBy { it.driverNumber }
            .mapValues { (_, laps) ->
                laps.associateBy { it.lapNumber }
            }

        val stintsByDriver = openF1Data.stints
            .groupBy { it.driverNumber }
            .mapValues { (_, stints) -> stints.sortedBy { it.stintNumber } }

        val pitStopsByDriver = openF1Data.pitStops
            .groupBy { it.driverNumber }
            .mapValues { (_, pitStops) -> pitStops.sortedBy { it.lapNumber } }

        // Group intervals by lap for gap information
        val intervalsByLap = openF1Data.intervals
            .groupBy { extractLapNumberFromInterval(it, lapsByDriver) }
            .mapValues { (_, intervals) ->
                intervals.associateBy { it.driverNumber }
            }

        // Group positions by lap - this is the key data for real positions!
//        val positionsByLap = openF1Data.positions
//            .groupBy { extractLapNumberFromPosition(it, lapsByDriver) }
//            .mapValues { (_, positions) ->
//                positions.associateBy { it.driverNumber }
//            }

        val allLaps = openF1Data.laps
            .map { it.lapNumber }
            .distinct()
            .sorted()

        return allLaps.mapNotNull { lap ->
            val driverPositions = createDriverPositionsForLap(
                lap = lap,
                drivers = drivers,
                lapsByDriver = lapsByDriver,
                stintsByDriver = stintsByDriver,
                pitStopsByDriver = pitStopsByDriver,
                intervalsByLap = intervalsByLap,
//                positionsByLap = positionsByLap
            )

            if (driverPositions.isNotEmpty()) {
                F1ReplayFrame(
                    lapNumber = lap,
                    elapsedTime = calculateElapsedTimeForLap(lap, lapsByDriver),
                    driverPositions = driverPositions.sortedBy { it.position }
                )
            } else null
        }
    }

    private fun createDriverPositionsForLap(
        lap: Int,
        drivers: Map<Int, F1Driver>,
        lapsByDriver: Map<Int, Map<Int, OpenF1LapResponse>>,
        stintsByDriver: Map<Int, List<OpenF1StintResponse>>,
        pitStopsByDriver: Map<Int, List<OpenF1PitResponse>>,
        intervalsByLap: Map<Int, Map<Int, OpenF1IntervalResponse>>
    ): List<F1DriverPosition> {
        val driversWithCumulativeTime = drivers.keys.mapNotNull { driverNumber ->
            val driverLaps = lapsByDriver[driverNumber] ?: emptyMap()

            val cumulativeTime = (1..lap).sumOf { lapNum ->
                driverLaps[lapNum]?.lapDuration ?: 90.0 // Default 90s if no data
            }

            val currentLapData = driverLaps[lap]

            Triple(driverNumber, cumulativeTime, currentLapData)
        }.sortedBy { it.second } // Sort by cumulative race time for realistic positions
        val gap = intervalsByLap[lap].toString()

        return if (driversWithCumulativeTime.isEmpty()) {
            drivers.toList().mapIndexed { index, (driverNumber, _) ->
                F1DriverPosition(
                    driverNumber = driverNumber,
                    position = index + 1,
                    gap = gap,
                    lapTime = "--:--.---",
                    tyre = getTyreCompoundForLap(lap, stintsByDriver[driverNumber] ?: emptyList()),
                    pitStops = getPitStopCountAtLap(lap, pitStopsByDriver[driverNumber] ?: emptyList())
                )
            }
        } else {
            val leaderTime = driversWithCumulativeTime.firstOrNull()?.second ?: 0.0

            driversWithCumulativeTime.mapIndexed { index, (driverNumber, cumulativeTime, currentLapData) ->
                val tyreCompound = getTyreCompoundForLap(lap, stintsByDriver[driverNumber] ?: emptyList())
                val pitStopCount = getPitStopCountAtLap(lap, pitStopsByDriver[driverNumber] ?: emptyList())
                val intervalData = intervalsByLap[lap]?.get(driverNumber)

                val gapToLeader = if (index == 0) {
                    "0.000"
                } else {
                    val timeDiff = cumulativeTime - leaderTime
                    "+${timeDiff}"
                }

                F1DriverPosition(
                    driverNumber = driverNumber,
                    position = index + 1,
                    gap = intervalData?.gap ?: gapToLeader,
                    lapTime = formatLapTime(currentLapData?.lapDuration),
                    tyre = tyreCompound,
                    pitStops = pitStopCount
                )
            }
        }
    }

    private fun getTyreCompoundForLap(
        lap: Int,
        stints: List<OpenF1StintResponse>
    ): String {
        val currentStint = stints.find { stint ->
            val lapStart = stint.lapStart ?: 1
            val lapEnd = stint.lapEnd ?: Int.MAX_VALUE
            lap in lapStart..lapEnd
        }
        return currentStint?.compound ?: ""
    }

    private fun getPitStopCountAtLap(
        lap: Int,
        pitStops: List<OpenF1PitResponse>
    ): Int {
        return pitStops.count { it.lapNumber <= lap }
    }

    private fun formatLapTime(lapDuration: Double?): String {
        if (lapDuration == null || lapDuration <= 0) return "--:--.---"

        val minutes = (lapDuration / 60).toInt()
        val seconds = lapDuration % 60
        val wholeSeconds = seconds.toInt()
        val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()

        return "${minutes}:${wholeSeconds.toString().padStart(2, '0')}.${milliseconds.toString().padStart(3, '0')}"
    }

    private fun extractLapNumberFromInterval(
        interval: OpenF1IntervalResponse,
        lapsByDriver: Map<Int, Map<Int, OpenF1LapResponse>>
    ): Int {
        val driverLaps = lapsByDriver[interval.driverNumber] ?: return 1

        val availableLaps = driverLaps.keys.sorted()
        if (availableLaps.isEmpty()) return 1

        return availableLaps.random()
    }

    private fun createFallbackFramesWithStints(
        drivers: Map<Int, F1Driver>,
        stints: List<OpenF1StintResponse>,
        pitStops: List<OpenF1PitResponse>
    ): List<F1ReplayFrame> {
        val stintsByDriver = stints.groupBy { it.driverNumber }
            .mapValues { (_, stints) -> stints.sortedBy { it.stintNumber } }

        val pitStopsByDriver = pitStops.groupBy { it.driverNumber }
            .mapValues { (_, pitStops) -> pitStops.sortedBy { it.lapNumber } }

        val maxLapFromStints = stints.mapNotNull { it.lapEnd }.maxOrNull() ?: 50
        val totalLaps = maxLapFromStints.coerceAtLeast(50)

        println("Creating fallback frames with ${totalLaps} laps using stint data")

        return (1..totalLaps).map { lap ->
            val driverPositions = drivers.toList().mapIndexed { index, (driverNumber, _) ->
                val gapVariation = kotlin.random.Random.nextDouble(-1.0, 2.0)
                val baseGap = index * 3.0 + gapVariation

                val tyreCompound = getTyreCompoundForLap(lap, stintsByDriver[driverNumber] ?: emptyList())
                val pitStopCount = getPitStopCountAtLap(lap, pitStopsByDriver[driverNumber] ?: emptyList())

                F1DriverPosition(
                    driverNumber = driverNumber,
                    position = index + 1,
                    gap = if (index == 0) "0.000" else "+${baseGap.coerceAtLeast(0.0)}",
                    lapTime = generateRandomLapTime(),
                    tyre = tyreCompound,
                    pitStops = pitStopCount
                )
            }

            F1ReplayFrame(
                lapNumber = lap,
                elapsedTime = lap * 90.0,
                driverPositions = driverPositions
            )
        }
    }

    private fun createFallbackFrames(drivers: Map<Int, F1Driver>): List<F1ReplayFrame> {
        val totalLaps = 50
        return (1..totalLaps).map { lap ->
            val driverPositions = drivers.toList().mapIndexed { index, (driverNumber, _) ->
                F1DriverPosition(
                    driverNumber = driverNumber,
                    position = index + 1,
                    gap = if (index == 0) "0.000" else "+${index * 3.0}",
                    lapTime = generateRandomLapTime(),
                    tyre = "",
                    pitStops = if (lap > 20) 1 else 0
                )
            }

            F1ReplayFrame(
                lapNumber = lap,
                elapsedTime = lap * 90.0,
                driverPositions = driverPositions
            )
        }
    }

    private fun generateRandomLapTime(): String {
        val baseTimeSeconds = 80 + kotlin.random.Random.nextDouble() * 15
        val minutes = (baseTimeSeconds / 60).toInt()
        val seconds = baseTimeSeconds % 60
        val wholeSeconds = seconds.toInt()
        val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()

        return "${minutes}:${wholeSeconds.toString().padStart(2, '0')}.${milliseconds.toString().padStart(3, '0')}"
    }

    private fun calculateElapsedTimeForLap(lap: Int, lapsByDriver: Map<Int, Map<Int, OpenF1LapResponse>>): Double {
        val averageLapDuration = lapsByDriver.values
            .mapNotNull { driverLaps -> driverLaps[lap]?.lapDuration }
            .takeIf { it.isNotEmpty() }
            ?.average()

        return if (averageLapDuration != null) {
            lap * averageLapDuration
        } else {
            lap * 90.0
        }
    }

    private fun calculateRaceDuration(frames: List<F1ReplayFrame>): Double {
        return frames.lastOrNull()?.elapsedTime ?: 0.0
    }
}