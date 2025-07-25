package com.jskinner.f1dash.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class F1PositionApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("driver_number")
    val driverNumber: Int,
    @SerialName("position")
    val position: Int,
    @SerialName("date")
    val date: String? = null
)