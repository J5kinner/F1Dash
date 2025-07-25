package com.jskinner.f1dash.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class F1StintApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("driver_number")
    val driverNumber: Int,
    @SerialName("compound")
    val compound: String? = null,
    @SerialName("lap_start")
    val lapStart: Int? = null,
    @SerialName("lap_end")
    val lapEnd: Int? = null,
    @SerialName("tyre_age_at_start")
    val tyreAgeAtStart: Int? = null,
    @SerialName("stint_number")
    val stintNumber: Int? = null
)