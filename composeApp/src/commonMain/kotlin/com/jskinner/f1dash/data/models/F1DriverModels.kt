package com.jskinner.f1dash.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class F1DriverApiResponse(
    @SerialName("meeting_key")
    val meetingKey: Int?,
    @SerialName("session_key")
    val sessionKey: Int?,
    @SerialName("driver_number")
    val driverNumber: Int,
    @SerialName("broadcast_name")
    val broadcastName: String?,
    @SerialName("full_name")
    val fullName: String?,
    @SerialName("name_acronym")
    val nameAcronym: String?,
    @SerialName("team_name")
    val teamName: String?,
    @SerialName("team_colour")
    val teamColour: String?,
    @SerialName("first_name")
    val firstName: String?,
    @SerialName("last_name")
    val lastName: String?,
    @SerialName("headshot_url")
    val headshotUrl: String?,
    @SerialName("country_code")
    val countryCode: String?
)