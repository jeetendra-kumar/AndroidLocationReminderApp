package com.jeet.androidreminderapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.model.TriggerType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val radiusMeters: Float,
    val triggerType: String,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val createdAt: Long,
    val lastTriggeredAt: Long?,
    val triggerCount: Int,
    val checkFrequencyMinutes: Int,
    val notifyOnBadWeather: Boolean
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    latitude = latitude,
    longitude = longitude,
    locationName = locationName,
    radiusMeters = radiusMeters,
    triggerType = runCatching { TriggerType.valueOf(triggerType) }.getOrDefault(TriggerType.ON_ARRIVAL),
    isActive = isActive,
    isCompleted = isCompleted,
    createdAt = createdAt,
    lastTriggeredAt = lastTriggeredAt,
    triggerCount = triggerCount,
    checkFrequencyMinutes = checkFrequencyMinutes,
    notifyOnBadWeather = notifyOnBadWeather
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    latitude = latitude,
    longitude = longitude,
    locationName = locationName,
    radiusMeters = radiusMeters,
    triggerType = triggerType.name,
    isActive = isActive,
    isCompleted = isCompleted,
    createdAt = createdAt,
    lastTriggeredAt = lastTriggeredAt,
    triggerCount = triggerCount,
    checkFrequencyMinutes = checkFrequencyMinutes,
    notifyOnBadWeather = notifyOnBadWeather
)
