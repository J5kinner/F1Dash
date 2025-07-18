package com.jskinner.f1dash.domain.models

import com.jskinner.f1dash.data.models.F1DriverApiResponse

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