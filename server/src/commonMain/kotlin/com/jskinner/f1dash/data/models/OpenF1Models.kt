package com.jskinner.f1dash.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenF1PositionResponse(
    val date: String,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("meeting_key") val meetingKey: Int,
    val position: Int,
    @SerialName("session_key") val sessionKey: Int
)

@Serializable
data class OpenF1LapResponse(
    @SerialName("date_start") val dateStart: String?,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("duration_sector_1") val durationSector1: Double?,
    @SerialName("duration_sector_2") val durationSector2: Double?,
    @SerialName("duration_sector_3") val durationSector3: Double?,
    @SerialName("i1_speed") val i1Speed: Int?,
    @SerialName("i2_speed") val i2Speed: Int?,
    @SerialName("is_pit_out_lap") val isPitOutLap: Boolean,
    @SerialName("lap_duration") val lapDuration: Double?,
    @SerialName("lap_number") val lapNumber: Int,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("st_speed") val stSpeed: Int?
)

@Serializable
data class OpenF1IntervalResponse(
    val date: String,
    @SerialName("driver_number") val driverNumber: Int,
    val gap: String?,
    val interval: String?,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("session_key") val sessionKey: Int
)

@Serializable
data class OpenF1StintResponse(
    @SerialName("compound") val compound: String,
    @SerialName("driver_number") val driverNumber: Int,
    @SerialName("lap_end") val lapEnd: Int?,
    @SerialName("lap_start") val lapStart: Int?,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("session_key") val sessionKey: Int,
    @SerialName("stint_number") val stintNumber: Int,
    @SerialName("tyre_age_at_start") val tyreAgeAtStart: Int
)

@Serializable
data class OpenF1PitResponse(
    val date: String,
    @SerialName("driver_number") val driverNumber: Int,
    val duration: Double,
    @SerialName("lap_number") val lapNumber: Int,
    @SerialName("meeting_key") val meetingKey: Int,
    @SerialName("pit_duration") val pitDuration: Double?,
    @SerialName("session_key") val sessionKey: Int
)