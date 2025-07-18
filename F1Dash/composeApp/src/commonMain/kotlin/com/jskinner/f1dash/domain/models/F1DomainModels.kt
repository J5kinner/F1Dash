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
    fullName = fullName,
    firstName = firstName,
    lastName = lastName,
    nameAcronym = nameAcronym,
    teamName = teamName,
    teamColour = teamColour,
    headshotUrl = headshotUrl,
    countryCode = countryCode,
    broadcastName = broadcastName
)