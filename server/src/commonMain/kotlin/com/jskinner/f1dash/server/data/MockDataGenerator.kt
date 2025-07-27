package com.jskinner.f1dash.server.data

import com.jskinner.f1dash.data.models.*
import kotlin.random.Random

object MockDataGenerator {

    private val drivers = listOf(
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 1,
            broadcastName = "M VERSTAPPEN",
            fullName = "Max VERSTAPPEN",
            nameAcronym = "VER",
            teamName = "Red Bull Racing Honda RBPT",
            teamColour = "3671C6",
            firstName = "Max",
            lastName = "Verstappen",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/M/MAXVER01_Max_Verstappen/maxver01.png",
            countryCode = "NLD"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 81,
            broadcastName = "O PIASTRI",
            fullName = "Oscar PIASTRI",
            nameAcronym = "PIA",
            teamName = "McLaren Mercedes",
            teamColour = "FF8000",
            firstName = "Oscar",
            lastName = "Piastri",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/O/OSCPIA01_Oscar_Piastri/oscpia01.png",
            countryCode = "AUS"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 4,
            broadcastName = "L NORRIS",
            fullName = "Lando NORRIS",
            nameAcronym = "NOR",
            teamName = "McLaren Mercedes",
            teamColour = "FF8000",
            firstName = "Lando",
            lastName = "Norris",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/L/LANNOR01_Lando_Norris/lannor01.png",
            countryCode = "GBR"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 16,
            broadcastName = "C LECLERC",
            fullName = "Charles LECLERC",
            nameAcronym = "LEC",
            teamName = "Ferrari",
            teamColour = "E80020",
            firstName = "Charles",
            lastName = "Leclerc",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/C/CHALEC01_Charles_Leclerc/chalec01.png",
            countryCode = "MCO"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 55,
            broadcastName = "C SAINZ",
            fullName = "Carlos SAINZ",
            nameAcronym = "SAI",
            teamName = "Ferrari",
            teamColour = "E80020",
            firstName = "Carlos",
            lastName = "Sainz",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/C/CARSAI01_Carlos_Sainz/carsai01.png",
            countryCode = "ESP"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 63,
            broadcastName = "G RUSSELL",
            fullName = "George RUSSELL",
            nameAcronym = "RUS",
            teamName = "Mercedes",
            teamColour = "27F4D2",
            firstName = "George",
            lastName = "Russell",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/G/GEORUS01_George_Russell/georus01.png",
            countryCode = "GBR"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 44,
            broadcastName = "L HAMILTON",
            fullName = "Lewis HAMILTON",
            nameAcronym = "HAM",
            teamName = "Mercedes",
            teamColour = "27F4D2",
            firstName = "Lewis",
            lastName = "Hamilton",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/L/LEWHAM01_Lewis_Hamilton/lewham01.png",
            countryCode = "GBR"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 11,
            broadcastName = "S PEREZ",
            fullName = "Sergio PEREZ",
            nameAcronym = "PER",
            teamName = "Red Bull Racing Honda RBPT",
            teamColour = "3671C6",
            firstName = "Sergio",
            lastName = "Perez",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/S/SERPER01_Sergio_Perez/serper01.png",
            countryCode = "MEX"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 14,
            broadcastName = "F ALONSO",
            fullName = "Fernando ALONSO",
            nameAcronym = "ALO",
            teamName = "Aston Martin Aramco Mercedes",
            teamColour = "229971",
            firstName = "Fernando",
            lastName = "Alonso",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/F/FERALO01_Fernando_Alonso/feralo01.png",
            countryCode = "ESP"
        ),
        F1DriverApiResponse(
            meetingKey = 1250,
            sessionKey = 9250,
            driverNumber = 18,
            broadcastName = "L STROLL",
            fullName = "Lance STROLL",
            nameAcronym = "STR",
            teamName = "Aston Martin Aramco Mercedes",
            teamColour = "229971",
            firstName = "Lance",
            lastName = "Stroll",
            headshotUrl = "https://www.formula1.com/content/dam/fom-website/drivers/L/LANSTR01_Lance_Stroll/lanstr01.png",
            countryCode = "CAN"
        )
    )

    private val sessions = listOf(
        // 2025 F1 Season - Bahrain GP
        F1SessionApiResponse(
            sessionKey = 9250,
            meetingKey = 1250,
            sessionName = "Race",
            sessionType = "Race",
            dateStart = "2025-03-16T15:00:00",
            dateEnd = "2025-03-16T17:00:00",
            gmtOffset = "+03:00",
            location = "Sakhir",
            countryName = "Bahrain",
            countryCode = "BHR",
            circuitName = "Bahrain International Circuit",
            circuitShortName = "Bahrain",
            year = 2025
        ),
        F1SessionApiResponse(
            sessionKey = 9249,
            meetingKey = 1250,
            sessionName = "Qualifying",
            sessionType = "Qualifying",
            dateStart = "2025-03-15T18:00:00",
            dateEnd = "2025-03-15T19:00:00",
            gmtOffset = "+03:00",
            location = "Sakhir",
            countryName = "Bahrain",
            countryCode = "BHR",
            circuitName = "Bahrain International Circuit",
            circuitShortName = "Bahrain",
            year = 2025
        ),
        F1SessionApiResponse(
            sessionKey = 9248,
            meetingKey = 1250,
            sessionName = "Sprint",
            sessionType = "Sprint",
            dateStart = "2025-03-15T14:30:00",
            dateEnd = "2025-03-15T15:00:00",
            gmtOffset = "+03:00",
            location = "Sakhir",
            countryName = "Bahrain",
            countryCode = "BHR",
            circuitName = "Bahrain International Circuit",
            circuitShortName = "Bahrain",
            year = 2025
        ),

        // 2025 F1 Season - Saudi Arabia GP
        F1SessionApiResponse(
            sessionKey = 9247,
            meetingKey = 1249,
            sessionName = "Race",
            sessionType = "Race",
            dateStart = "2025-03-09T20:00:00",
            dateEnd = "2025-03-09T22:00:00",
            gmtOffset = "+03:00",
            location = "Jeddah",
            countryName = "Saudi Arabia",
            countryCode = "SAU",
            circuitName = "Jeddah Corniche Circuit",
            circuitShortName = "Jeddah",
            year = 2025
        ),
        F1SessionApiResponse(
            sessionKey = 9246,
            meetingKey = 1249,
            sessionName = "Qualifying",
            sessionType = "Qualifying",
            dateStart = "2025-03-08T20:00:00",
            dateEnd = "2025-03-08T21:00:00",
            gmtOffset = "+03:00",
            location = "Jeddah",
            countryName = "Saudi Arabia",
            countryCode = "SAU",
            circuitName = "Jeddah Corniche Circuit",
            circuitShortName = "Jeddah",
            year = 2025
        ),

        // Keep some 2024 sessions for backward compatibility
        F1SessionApiResponse(
            sessionKey = 9158,
            meetingKey = 1234,
            sessionName = "Race",
            sessionType = "Race",
            dateStart = "2024-12-08T15:00:00",
            dateEnd = "2024-12-08T17:00:00",
            gmtOffset = "+04:00",
            location = "Abu Dhabi",
            countryName = "United Arab Emirates",
            countryCode = "ARE",
            circuitName = "Yas Marina Circuit",
            circuitShortName = "Yas Marina",
            year = 2024
        ),
        F1SessionApiResponse(
            sessionKey = 9157,
            meetingKey = 1234,
            sessionName = "Qualifying",
            sessionType = "Qualifying",
            dateStart = "2024-12-07T18:00:00",
            dateEnd = "2024-12-07T19:00:00",
            gmtOffset = "+04:00",
            location = "Abu Dhabi",
            countryName = "United Arab Emirates",
            countryCode = "ARE",
            circuitName = "Yas Marina Circuit",
            circuitShortName = "Yas Marina",
            year = 2024
        )
    )

    fun getDrivers(sessionKey: Int? = null, driverNumber: Int? = null): List<F1DriverApiResponse> {
        var result = drivers

        sessionKey?.let { key ->
            result = result.map { it.copy(sessionKey = key) }
        }

        driverNumber?.let { number ->
            result = result.filter { it.driverNumber == number }
        }

        return result
    }

    fun getSessions(
        year: Int? = null,
        sessionType: String? = null,
        sessionKey: Int? = null
    ): List<F1SessionApiResponse> {
        var result = sessions

        year?.let { yr ->
            result = result.filter { it.year == yr }
        }

        sessionType?.let { type ->
            result = result.filter { it.sessionType?.equals(type, ignoreCase = true) == true }
        }

        sessionKey?.let { key ->
            result = result.filter { it.sessionKey == key }
        }

        return result
    }

    fun generatePositions(
        sessionKey: Int = 9250,
        driverNumber: Int? = null,
        lapNumber: Int? = null
    ): List<OpenF1PositionResponse> {
        val selectedDrivers = if (driverNumber != null) {
            drivers.filter { it.driverNumber == driverNumber }
        } else {
            drivers.take(10)
        }

        val positions = mutableListOf<OpenF1PositionResponse>()
        val maxLaps = 50

        // Create initial grid positions (shuffled for realism)
        val gridPositions = selectedDrivers.mapIndexed { index, driver ->
            driver to (index + 1)
        }.toMutableList()

        // Simulate race dynamics
        val lapsToGenerate = if (lapNumber != null) {
            listOf(lapNumber)
        } else {
            (1..maxLaps).toList()
        }

        // DNF scenarios - some drivers retire during the race
        val dnfDrivers = mutableSetOf<Int>()
        val dnfLaps = mapOf(
            drivers[8].driverNumber to 15,  // Alonso DNF on lap 15
            drivers[9].driverNumber to 32   // Stroll DNF on lap 32
        )

        lapsToGenerate.forEach { lap ->
            // Apply DNFs
            dnfLaps.forEach { (driverNum, dnfLap) ->
                if (lap >= dnfLap) {
                    dnfDrivers.add(driverNum)
                }
            }

            // Simulate position changes (overtaking)
            if (lap > 1 && Random.nextDouble() < 0.3) { // 30% chance of position change per lap
                val activeDrivers = gridPositions.filter { it.first.driverNumber !in dnfDrivers }
                if (activeDrivers.size >= 2) {
                    // Random overtake between adjacent positions
                    val swapIndex = Random.nextInt(0, activeDrivers.size - 1)
                    val driver1Pos =
                        gridPositions.indexOfFirst { it.first.driverNumber == activeDrivers[swapIndex].first.driverNumber }
                    val driver2Pos =
                        gridPositions.indexOfFirst { it.first.driverNumber == activeDrivers[swapIndex + 1].first.driverNumber }

                    if (driver1Pos != -1 && driver2Pos != -1) {
                        val temp = gridPositions[driver1Pos].second
                        gridPositions[driver1Pos] = gridPositions[driver1Pos].first to gridPositions[driver2Pos].second
                        gridPositions[driver2Pos] = gridPositions[driver2Pos].first to temp
                    }
                }
            }

            // Add special overtaking opportunities during pit windows (laps 15-25, 35-45)
            if ((lap in 15..25 || lap in 35..45) && Random.nextDouble() < 0.5) {
                val activeDrivers = gridPositions.filter { it.first.driverNumber !in dnfDrivers }
                if (activeDrivers.size >= 3) {
                    // Bigger position swaps during pit windows
                    val positions = activeDrivers.map { it.second }.sorted().toMutableList()
                    positions.shuffle(Random)
                    activeDrivers.forEachIndexed { index, (driver, _) ->
                        val driverIndex = gridPositions.indexOfFirst { it.first.driverNumber == driver.driverNumber }
                        if (driverIndex != -1) {
                            gridPositions[driverIndex] = driver to positions[index]
                        }
                    }
                }
            }

            // Generate position data for this lap
            gridPositions.forEach { (driver, position) ->
                if (driver.driverNumber !in dnfDrivers) {
                    positions.add(
                        OpenF1PositionResponse(
                            date = "2025-03-16T15:${(lap * 2).toString().padStart(2, '0')}:00.000Z",
                            driverNumber = driver.driverNumber,
                            meetingKey = 1250,
                            position = position,
                            sessionKey = sessionKey
                        )
                    )
                }
            }
        }

        return positions.sortedWith(compareBy({ it.date }, { it.position }))
    }

    fun generateLaps(sessionKey: Int, driverNumber: Int? = null, lapNumber: Int? = null): List<OpenF1LapResponse> {
        val selectedDrivers = if (driverNumber != null) {
            drivers.filter { it.driverNumber == driverNumber }
        } else {
            drivers.take(10)
        }

        val laps = mutableListOf<OpenF1LapResponse>()
        val maxLaps = 50

        // DNF scenarios
        val dnfDrivers = mapOf(
            drivers[8].driverNumber to 15,  // Alonso DNF on lap 15
            drivers[9].driverNumber to 32   // Stroll DNF on lap 32
        )

        selectedDrivers.forEach { driver ->
            val lapsToGenerate = if (lapNumber != null) {
                listOf(lapNumber)
            } else {
                (1..maxLaps).toList()
            }

            // Driver performance characteristics
            val driverSkill = when (driver.driverNumber) {
                1, 81, 4 -> 1.0   // Verstappen, Piastri, Norris - top performers
                16, 55 -> 0.95    // Leclerc, Sainz - slightly slower
                63, 44 -> 0.92    // Russell, Hamilton - competitive but aging
                else -> 0.88      // Other drivers
            }

            // Base lap time varies by driver skill
            val baseTime = 85.0 / driverSkill

            lapsToGenerate.forEach { lap ->
                // Check if driver has DNF'd
                val dnfLap = dnfDrivers[driver.driverNumber]
                if (dnfLap != null && lap >= dnfLap) {
                    return@forEach // Skip generating laps after DNF
                }

                // Tire degradation - times get slower as tires age
                val tireDegradation = when {
                    lap <= 15 -> 0.0  // Fresh tires
                    lap <= 30 -> (lap - 15) * 0.1  // Medium degradation
                    else -> (lap - 30) * 0.2 + 1.5  // High degradation
                }

                // Traffic and race conditions
                val trafficEffect = if (Random.nextDouble() < 0.2) Random.nextDouble(0.5, 2.0) else 0.0

                // Safety car periods (slower laps)
                val safetyCar = if (lap in listOf(18, 19, 20, 35, 36)) 25.0 else 0.0

                // Pit stops (much slower lap)
                val isPitLap = (lap == 16 && Random.nextDouble() < 0.6) ||
                        (lap == 36 && Random.nextDouble() < 0.8)
                val pitStopTime = if (isPitLap) Random.nextDouble(18.0, 25.0) else 0.0

                val finalLapTime =
                    baseTime + tireDegradation + trafficEffect + safetyCar + pitStopTime + Random.nextDouble(-1.0, 1.0)

                // Sector times should add up roughly to lap time
                val sector1Base = finalLapTime / 3.2
                val sector2Base = finalLapTime / 2.8
                val sector3Base = finalLapTime / 3.0

                laps.add(
                    OpenF1LapResponse(
                        dateStart = "2025-03-16T15:${(lap * 2).toString().padStart(2, '0')}:00.000Z",
                        driverNumber = driver.driverNumber,
                        durationSector1 = sector1Base + Random.nextDouble(-0.5, 0.5),
                        durationSector2 = sector2Base + Random.nextDouble(-0.5, 0.5),
                        durationSector3 = sector3Base + Random.nextDouble(-0.5, 0.5),
                        i1Speed = if (safetyCar > 0) Random.nextInt(180, 220) else Random.nextInt(280, 330),
                        i2Speed = if (safetyCar > 0) Random.nextInt(160, 200) else Random.nextInt(260, 310),
                        isPitOutLap = isPitLap,
                        lapDuration = finalLapTime,
                        lapNumber = lap,
                        meetingKey = 1250,
                        sessionKey = sessionKey,
                        stSpeed = if (safetyCar > 0) Random.nextInt(200, 250) else Random.nextInt(310, 350)
                    )
                )
            }
        }

        return laps
    }

    fun generateIntervals(sessionKey: Int, driverNumber: Int? = null): List<OpenF1IntervalResponse> {
        val selectedDrivers = if (driverNumber != null) {
            drivers.filter { it.driverNumber == driverNumber }
        } else {
            drivers.take(10)
        }

        return selectedDrivers.mapIndexed { index, driver ->
            OpenF1IntervalResponse(
                date = "2024-12-08T15:30:00.000Z",
                driverNumber = driver.driverNumber,
                gap = if (index == 0) 0.0 else (index * 5.0) + Random.nextDouble(-2.0, 2.0),
                interval = if (index == 0) null else Random.nextDouble(2.0, 8.0),
                meetingKey = 1234,
                sessionKey = sessionKey
            )
        }
    }

    fun generateStints(sessionKey: Int, driverNumber: Int? = null): List<OpenF1StintResponse> {
        val selectedDrivers = if (driverNumber != null) {
            drivers.filter { it.driverNumber == driverNumber }
        } else {
            drivers.take(10)
        }

        val compounds = listOf("SOFT", "MEDIUM", "HARD")
        val stints = mutableListOf<OpenF1StintResponse>()

        selectedDrivers.forEach { driver ->
            listOf(1, 2, 3).forEach { stintNumber ->
                stints.add(
                    OpenF1StintResponse(
                        compound = compounds.random(),
                        driverNumber = driver.driverNumber,
                        lapEnd = if (stintNumber == 3) null else (stintNumber * 15) + Random.nextInt(0, 6),
                        lapStart = ((stintNumber - 1) * 15) + 1,
                        meetingKey = 1234,
                        sessionKey = sessionKey,
                        stintNumber = stintNumber,
                        tyreAgeAtStart = Random.nextInt(0, 11)
                    )
                )
            }
        }

        return stints
    }

    fun generatePitStops(sessionKey: Int, driverNumber: Int? = null): List<OpenF1PitResponse> {
        val selectedDrivers = if (driverNumber != null) {
            drivers.filter { it.driverNumber == driverNumber }
        } else {
            drivers.take(10)
        }

        return selectedDrivers.flatMap { driver ->
            listOf(15, 35).map { lap ->
                OpenF1PitResponse(
                    date = "2024-12-08T15:${(lap * 2).toString().padStart(2, '0')}:00.000Z",
                    driverNumber = driver.driverNumber,
                    duration = Random.nextDouble(2.5, 4.5),
                    lapNumber = lap,
                    meetingKey = 1234,
                    pitDuration = Random.nextDouble(20.0, 35.0),
                    sessionKey = sessionKey
                )
            }
        }
    }

    fun generateSessionResults(sessionKey: Int): List<F1SessionResultApiResponse> {
        val results = mutableListOf<F1SessionResultApiResponse>()
        val raceDrivers = drivers.take(10)

        // DNF scenarios
        val dnfDrivers = mapOf(
            drivers[8].driverNumber to 15,  // Alonso DNF on lap 15 (engine failure)
            drivers[9].driverNumber to 32   // Stroll DNF on lap 32 (collision damage)
        )

        // Create results for finishing drivers first
        val finishingDrivers = raceDrivers.filter { it.driverNumber !in dnfDrivers.keys }

        finishingDrivers.forEachIndexed { index, driver ->
            // Add some randomness to final positions
            val basePosition = index + 1
            val finalPosition = if (Random.nextDouble() < 0.3) {
                // 30% chance of slight position change for more realistic results
                (basePosition + Random.nextInt(-1, 2)).coerceIn(1, finishingDrivers.size)
            } else {
                basePosition
            }

            results.add(
                F1SessionResultApiResponse(
                    sessionKey = sessionKey,
                    meetingKey = 1250,
                    driverNumber = driver.driverNumber,
                    position = finalPosition,
                    classified = true,
                    dnf = false,
                    dns = false,
                    disqualified = false,
                    raceTime = if (finalPosition == 1) 5400.0 else 5400.0 + (finalPosition * 8.0) + Random.nextDouble(
                        0.0,
                        15.0
                    ),
                    gapToLeader = if (finalPosition == 1) 0.0 else (finalPosition * 6.0) + Random.nextDouble(0.0, 12.0),
                    lapsCompleted = 50
                )
            )
        }

        // Add DNF drivers at the bottom
        dnfDrivers.forEach { (driverNumber, dnfLap) ->
            val driver = raceDrivers.find { it.driverNumber == driverNumber }
            if (driver != null) {
                results.add(
                    F1SessionResultApiResponse(
                        sessionKey = sessionKey,
                        meetingKey = 1250,
                        driverNumber = driverNumber,
                        position = results.size + 1, // Bottom of results
                        classified = false,
                        dnf = true,
                        dns = false,
                        disqualified = false,
                        raceTime = null, // No finish time for DNF
                        gapToLeader = null,
                        lapsCompleted = dnfLap - 1 // Completed laps before DNF
                    )
                )
            }
        }

        return results.sortedBy { it.position }
    }
}