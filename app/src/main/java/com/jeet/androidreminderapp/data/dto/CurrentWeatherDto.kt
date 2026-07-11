package com.jeet.androidreminderapp.data.dto

import com.google.gson.annotations.SerializedName

data class CurrentWeatherDto(
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)
