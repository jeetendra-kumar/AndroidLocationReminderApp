package com.jeet.androidreminderapp.domain.repository

import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeActiveTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun upsertTask(task: Task): Long
    suspend fun deleteTask(task: Task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean)
    suspend fun setTaskActive(id: Long, active: Boolean)
    suspend fun markTriggered(id: Long, timestamp: Long)
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<Weather>
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<String>
}