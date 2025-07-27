package com.jskinner.f1dash.domain.models

import com.jskinner.f1dash.data.models.*

data class F1Driver(
    val driverNumber: Int,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val nameAcronym: String,
    val teamName: String,
    val teamColour: String,
    val headshotUrl: String?,
    val countryCode: String,
    val broadcastName: String
)

data class F1Session(
    val sessionKey: Int,
    val meetingKey: Int,
    val sessionName: String,
    val sessionType: String,
    val dateStart: String,
    val dateEnd: String,
    val location: String,
    val countryName: String,
    val countryCode: String,
    val circuitName: String,
    val circuitShortName: String,
    val year: Int
)


data class F1SessionResult(
    val sessionKey: Int,
    val meetingKey: Int,
    val driverNumber: Int,
    val position: Int,
    val classified: Boolean,
    val dnf: Boolean,
    val dns: Boolean,
    val disqualified: Boolean,
    val raceTime: Double?,
    val gapToLeader: Double?,
    val lapsCompleted: Int?
)

data class F1DriverResult(
    val driver: F1Driver,
    val position: Int,
    val lapTime: String,
    val gap: String = "",
    val status: String = "Running",
    val currentTyre: String = "UNKNOWN"
)

data class F1RaceData(
    val session: F1Session,
    val driverResults: List<F1DriverResult>,
    val fastestLap: F1DriverResult?
)

fun F1DriverApiResponse.toDomainModel() = F1Driver(
    driverNumber = driverNumber,
    fullName = fullName ?: "Unknown Driver",
    firstName = firstName ?: "Unknown",
    lastName = lastName ?: "Driver",
    nameAcronym = nameAcronym ?: "UNK",
    teamName = teamName ?: "Unknown Team",
    teamColour = teamColour ?: "808080", // Default gray color
    headshotUrl = headshotUrl,
    countryCode = countryCode ?: "XX",
    broadcastName = broadcastName ?: fullName ?: "Unknown"
)

fun F1SessionApiResponse.toDomainModel() = F1Session(
    sessionKey = sessionKey,
    meetingKey = meetingKey,
    sessionName = sessionName ?: "Unknown Session",
    sessionType = sessionType ?: "Unknown",
    dateStart = dateStart ?: "",
    dateEnd = dateEnd ?: "",
    location = location ?: "Unknown Location",
    countryName = countryName ?: "Unknown Country",
    countryCode = countryCode ?: "XX",
    circuitName = getCircuitName(circuitName, circuitShortName, location, countryName, countryCode),
    circuitShortName = circuitShortName ?: circuitName ?: "Unknown",
    year = year ?: 2024
)

private fun getCircuitName(
    circuitName: String?,
    circuitShortName: String?,
    location: String?,
    countryName: String?,
    countryCode: String?
): String {
    // Try circuit name first
    if (!circuitName.isNullOrBlank() && circuitName != "null") {
        return circuitName
    }

    // Try circuit short name
    if (!circuitShortName.isNullOrBlank() && circuitShortName != "null") {
        return circuitShortName
    }

    // Try location
    if (!location.isNullOrBlank() && location != "null") {
        return location
    }

    // Try to map country code to known circuit names
    val knownCircuits = mapOf(
        "AUS" to "Albert Park Circuit",
        "SAU" to "Jeddah Corniche Circuit",
        "AUS" to "Albert Park Circuit",
        "BHR" to "Bahrain International Circuit",
        "CHN" to "Shanghai International Circuit",
        "JPN" to "Suzuka International Racing Course",
        "MCO" to "Circuit de Monaco",
        "CAN" to "Circuit Gilles Villeneuve",
        "ESP" to "Circuit de Barcelona-Catalunya",
        "AUT" to "Red Bull Ring",
        "GBR" to "Silverstone Circuit",
        "HUN" to "Hungaroring",
        "BEL" to "Circuit de Spa-Francorchamps",
        "NLD" to "Circuit Zandvoort",
        "ITA" to "Monza Circuit",
        "SGP" to "Marina Bay Street Circuit",
        "USA" to "Circuit of the Americas",
        "MEX" to "Autódromo Hermanos Rodríguez",
        "BRA" to "Interlagos Circuit",
        "QAT" to "Losail International Circuit",
        "UAE" to "Yas Marina Circuit"
    )

    countryCode?.let { code ->
        knownCircuits[code]?.let { return it }
    }

    // Try country name
    if (!countryName.isNullOrBlank() && countryName != "null") {
        return "$countryName Grand Prix"
    }

    return "Unknown Circuit"
}

fun F1SessionResultApiResponse.toDomainModel() = F1SessionResult(
    sessionKey = sessionKey,
    meetingKey = meetingKey,
    driverNumber = driverNumber,
    position = position,
    classified = classified,
    dnf = dnf,
    dns = dns,
    disqualified = disqualified,
    raceTime = raceTime,
    gapToLeader = gapToLeader,
    lapsCompleted = lapsCompleted
)

data class F1ReplayFrame(
    val lapNumber: Int,
    val elapsedTime: Double,
    val driverPositions: List<F1DriverPosition>
)

data class F1DriverPosition(
    val driverNumber: Int,
    val position: Int,
    val gap: String,
    val lapTime: String,
    val tyre: String,
    val pitStops: Int
)

data class F1RaceReplay(
    val session: F1Session,
    val frames: List<F1ReplayFrame>,
    val totalLaps: Int,
    val raceDuration: Double,
    val drivers: Map<Int, F1Driver> = emptyMap()
)

data class F1ReplayState(
    val isPlaying: Boolean = false,
    val currentFrameIndex: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val totalFrames: Int = 0
) {
    val progress: Float
        get() = if (totalFrames > 0) currentFrameIndex.toFloat() / totalFrames else 0f
}

fun Double.formatLapTime(): String {
    if (this <= 0) return "--:--"
    val totalSeconds = this
    val minutes = (totalSeconds / 60).toInt()
    val seconds = totalSeconds % 60
    val wholeSeconds = seconds.toInt()
    val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()
    val paddedSeconds = wholeSeconds.toString().padStart(2, '0')
    val paddedMilliseconds = milliseconds.toString().padStart(3, '0')
    return "$minutes:$paddedSeconds.$paddedMilliseconds"
}