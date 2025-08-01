package com.jskinner.f1dash.data

import com.jskinner.f1dash.domain.models.*
import kotlin.random.Random

object MockReplayGenerator {

    private val tyreCompounds = listOf("SOFT", "MEDIUM", "HARD")

    fun generateRaceReplay(
        session: F1Session,
        drivers: Map<Int, F1Driver>
    ): F1RaceReplay {
        val totalLaps = 50 + Random.nextInt(20) // 50-70 laps
        val frames = generateReplayFrames(totalLaps, drivers)
        val raceDuration = calculateRaceDuration(frames)

        return F1RaceReplay(
            session = session,
            frames = frames,
            totalLaps = totalLaps,
            raceDuration = raceDuration,
            drivers = drivers
        )
    }

    private fun generateReplayFrames(
        totalLaps: Int,
        drivers: Map<Int, F1Driver>
    ): List<F1ReplayFrame> {
        val frames = mutableListOf<F1ReplayFrame>()
        val driverList = drivers.values.take(20).toList()

        // Initialize starting positions
        var currentPositions = driverList.mapIndexed { index, driver ->
            driver.driverNumber to (index + 1)
        }.toMap().toMutableMap()

        for (lap in 1..totalLaps) {
            // Occasionally shuffle positions slightly for realism
            if (lap > 1 && Random.nextDouble() < 0.3) {
                shufflePositions(currentPositions)
            }

            val driverPositions = createDriverPositions(
                currentPositions,
                drivers,
                lap,
                totalLaps
            )

            val frame = F1ReplayFrame(
                lapNumber = lap,
                elapsedTime = calculateElapsedTime(lap),
                driverPositions = driverPositions.sortedBy { it.position }
            )

            frames.add(frame)
        }

        return frames
    }

    private fun shufflePositions(positions: MutableMap<Int, Int>) {
        val drivers = positions.keys.toList()
        if (drivers.size >= 2 && Random.nextDouble() < 0.5) {
            val driver1 = drivers.random()
            val pos1 = positions[driver1]!!

            // Find adjacent driver
            val adjacentPos = if (Random.nextBoolean() && pos1 > 1) pos1 - 1 else pos1 + 1
            val driver2 = positions.entries.find { it.value == adjacentPos }?.key

            if (driver2 != null) {
                positions[driver1] = adjacentPos
                positions[driver2] = pos1
            }
        }
    }

    private fun createDriverPositions(
        positions: Map<Int, Int>,
        drivers: Map<Int, F1Driver>,
        lap: Int,
        totalLaps: Int
    ): List<F1DriverPosition> {
        return positions.map { (driverNumber, position) ->
            val driver = drivers[driverNumber]!!

            F1DriverPosition(
                driverNumber = driverNumber,
                position = position,
                gap = generateGap(position, lap),
                lapTime = generateLapTime(driver, lap),
                tyre = generateTyreCompound(lap, totalLaps),
                pitStops = calculatePitStops(lap, totalLaps)
            )
        }
    }

    private fun generateGap(position: Int, lap: Int): String {
        return when (position) {
            1 -> "0.000"
            2 -> {
                val gap = Random.nextDouble() * 2 + 0.5
                "+${formatToThreeDecimals(gap)}"
            }

            else -> {
                val baseGap = (position - 1) * (Random.nextDouble() * 3 + 2)
                val variation = Random.nextDouble() * 5
                "+${formatToThreeDecimals(baseGap + variation)}"
            }
        }
    }

    private fun formatToThreeDecimals(value: Double): String {
        val rounded = (value * 1000).toInt() / 1000.0
        val intPart = rounded.toInt()
        val fractionalPart = ((rounded - intPart) * 1000).toInt()
        return "$intPart.${fractionalPart.toString().padStart(3, '0')}"
    }

    private fun generateLapTime(driver: F1Driver, lap: Int): String {
        val baseSeconds = 80 + Random.nextDouble() * 10
        val variation = Random.nextDouble() * 3 - 1.5
        val totalSeconds = baseSeconds + variation

        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        val wholeSeconds = seconds.toInt()
        val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()

        return "${minutes}:${wholeSeconds.toString().padStart(2, '0')}.${milliseconds.toString().padStart(3, '0')}"
    }

    private fun generateTyreCompound(lap: Int, totalLaps: Int): String {
        return when {
            lap <= totalLaps / 3 -> tyreCompounds.random()
            lap <= 2 * totalLaps / 3 -> if (Random.nextBoolean()) "MEDIUM" else "HARD"
            else -> if (Random.nextBoolean()) "SOFT" else "MEDIUM"
        }
    }

    private fun calculatePitStops(lap: Int, totalLaps: Int): Int {
        return when {
            lap <= totalLaps / 3 -> 0
            lap <= 2 * totalLaps / 3 -> if (Random.nextDouble() < 0.7) 1 else 0
            else -> if (Random.nextDouble() < 0.3) 2 else 1
        }
    }

    private fun calculateElapsedTime(lap: Int): Double {
        return lap * (85 + Random.nextDouble() * 10)
    }

    private fun calculateRaceDuration(frames: List<F1ReplayFrame>): Double {
        return frames.lastOrNull()?.elapsedTime ?: 0.0
    }
}