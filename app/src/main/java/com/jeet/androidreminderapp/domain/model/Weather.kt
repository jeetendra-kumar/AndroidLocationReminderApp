package com.jeet.androidreminderapp.domain.model

data class Weather(
    val temperatureCelsius: Double,
    val weatherCode: Int,
    val description: String,
    val isRainy: Boolean,
    val windSpeedKmh: Double
)