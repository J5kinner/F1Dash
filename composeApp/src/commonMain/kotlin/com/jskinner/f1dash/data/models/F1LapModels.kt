package com.jskinner.f1dash.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class F1LapApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("driver_number")
    val driverNumber: Int,
    @SerialName("lap_number")
    val lapNumber: Int,
    @SerialName("lap_duration")
    val lapDuration: Double? = null,
    @SerialName("segments_sector_1")
    val segmentsSector1: List<Int>? = null,
    @SerialName("segments_sector_2")
    val segmentsSector2: List<Int>? = null,
    @SerialName("segments_sector_3")
    val segmentsSector3: List<Int>? = null,
    @SerialName("is_pit_out_lap")
    val isPitOutLap: Boolean = false,
    @SerialName("date_start")
    val dateStart: String? = null
)