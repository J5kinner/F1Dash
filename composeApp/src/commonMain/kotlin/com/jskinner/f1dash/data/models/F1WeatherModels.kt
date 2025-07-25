package com.jskinner.f1dash.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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