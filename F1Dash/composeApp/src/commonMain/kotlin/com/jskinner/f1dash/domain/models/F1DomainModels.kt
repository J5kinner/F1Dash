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

data class F1Lap(
    val sessionKey: Int,
    val meetingKey: Int,
    val driverNumber: Int,
    val lapNumber: Int,
    val lapDuration: Double?,
    val isPitOutLap: Boolean,
    val dateStart: String
)

data class F1Position(
    val sessionKey: Int,
    val meetingKey: Int,
    val driverNumber: Int,
    val position: Int,
    val date: String
)

data class F1Weather(
    val sessionKey: Int,
    val meetingKey: Int,
    val airTemperature: Double,
    val humidity: Int,
    val pressure: Double,
    val rainfall: Int,
    val trackTemperature: Double,
    val windDirection: Int,
    val windSpeed: Double,
    val date: String
)

data class F1DriverResult(
    val driver: F1Driver,
    val position: Int,
    val lapTime: String,
    val gap: String = "",
    val status: String = "Running"
)

data class F1RaceData(
    val session: F1Session,
    val driverResults: List<F1DriverResult>,
    val weather: F1Weather?,
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
    circuitName = circuitName ?: "Unknown Circuit",
    circuitShortName = circuitShortName ?: "Unknown",
    year = year ?: 2024
)

fun F1LapApiResponse.toDomainModel() = F1Lap(
    sessionKey = sessionKey,
    meetingKey = meetingKey,
    driverNumber = driverNumber,
    lapNumber = lapNumber,
    lapDuration = lapDuration,
    isPitOutLap = isPitOutLap,
    dateStart = dateStart ?: ""
)

fun F1PositionApiResponse.toDomainModel() = F1Position(
    sessionKey = sessionKey,
    meetingKey = meetingKey,
    driverNumber = driverNumber,
    position = position,
    date = date ?: ""
)

fun F1WeatherApiResponse.toDomainModel() = F1Weather(
    sessionKey = sessionKey,
    meetingKey = meetingKey,
    airTemperature = airTemperature ?: 0.0,
    humidity = humidity ?: 0,
    pressure = pressure ?: 0.0,
    rainfall = rainfall ?: 0,
    trackTemperature = trackTemperature ?: 0.0,
    windDirection = windDirection ?: 0,
    windSpeed = windSpeed ?: 0.0,
    date = date ?: ""
)

fun Double.formatLapTime(): String {
    if (this <= 0) return "--:--"
    val totalSeconds = this
    val minutes = (totalSeconds / 60).toInt()
    val seconds = totalSeconds % 60
    return String.format("%d:%06.3f", minutes, seconds)
}