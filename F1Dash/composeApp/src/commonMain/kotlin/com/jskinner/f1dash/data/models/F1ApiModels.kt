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

@Serializable
data class F1WeatherApiResponse(
    @SerialName("session_key")
    val sessionKey: Int,
    @SerialName("meeting_key")
    val meetingKey: Int,
    @SerialName("air_temperature")
    val airTemperature: Double? = null,
    @SerialName("humidity")
    val humidity: Int? = null,
    @SerialName("pressure")
    val pressure: Double? = null,
    @SerialName("rainfall")
    val rainfall: Int? = null,
    @SerialName("track_temperature")
    val trackTemperature: Double? = null,
    @SerialName("wind_direction")
    val windDirection: Int? = null,
    @SerialName("wind_speed")
    val windSpeed: Double? = null,
    @SerialName("date")
    val date: String? = null
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}