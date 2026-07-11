package com.jeet.androidreminderapp.domain.model

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val locationName: String = "",
    val radiusMeters: Float = 200f,
    val triggerType: TriggerType = TriggerType.ON_ARRIVAL,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null,
    val triggerCount: Int = 0,
    val checkFrequencyMinutes: Int = 15,
    val notifyOnBadWeather: Boolean = false
)

enum class TriggerType {
    ON_ARRIVAL,   // Notify when entering the geofence
    ON_DEPARTURE, // Notify when leaving the geofence
    ON_DWELL      // Notify after staying in the geofence for a while
}