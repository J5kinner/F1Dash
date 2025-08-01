package com.jskinner.f1dash.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class F1SessionApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("session_name")
    val sessionName: String? = null,
    @SerialName("session_type")
    val sessionType: String? = null,
    @SerialName("date_start")
    val dateStart: String? = null,
    @SerialName("date_end")
    val dateEnd: String? = null,
    @SerialName("gmt_offset")
    val gmtOffset: String? = null,
    @SerialName("location")
    val location: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    @SerialName("circuit_name")
    val circuitName: String? = null,
    @SerialName("circuit_short_name")
    val circuitShortName: String? = null,
    @SerialName("year")
    val year: Int? = null
)

@Serializable
data class F1SessionResultApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("driver_number")
    val driverNumber: Int,
    @SerialName("position")
    val position: Int,
    @SerialName("classified")
    val classified: Boolean = true,
    @SerialName("dnf")
    val dnf: Boolean = false,
    @SerialName("dns")
    val dns: Boolean = false,
    @SerialName("disqualified")
    val disqualified: Boolean = false,
    @SerialName("race_time")
    val raceTime: Double? = null,
    @SerialName("gap_to_leader")
    val gapToLeader: String? = null,
    @SerialName("laps_completed")
    val lapsCompleted: Int? = null
)