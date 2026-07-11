package com.jeet.androidreminderapp.data.repository

import com.jeet.androidreminderapp.data.local.dao.TaskDao
import com.jeet.androidreminderapp.data.local.entity.toDomain
import com.jeet.androidreminderapp.data.local.entity.toEntity
import com.jeet.androidreminderapp.data.remote.GeoCodingApi
import com.jeet.androidreminderapp.data.remote.WeatherApi
import com.jeet.androidreminderapp.data.remote.WeatherCodeMapper
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.model.Weather
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val weatherApi: WeatherApi,
    private val geocodingApi: GeoCodingApi
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActiveTasks(): Flow<List<Task>> =
        taskDao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? = withContext(Dispatchers.IO) {
        taskDao.getById(id)?.toDomain()
    }

    override suspend fun upsertTask(task: Task): Long = withContext(Dispatchers.IO) {
        taskDao.upsert(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.delete(task.toEntity())
    }

    override suspend fun setTaskCompleted(id: Long, completed: Boolean) =
        withContext(Dispatchers.IO) {
            taskDao.setCompleted(id, completed)
        }

    override suspend fun setTaskActive(id: Long, active: Boolean) = withContext(Dispatchers.IO) {
        taskDao.setActive(id, active)
    }

    override suspend fun markTriggered(id: Long, timestamp: Long) = withContext(Dispatchers.IO) {
        taskDao.markTriggered(id, timestamp)
    }

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Result<Weather> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = weatherApi.getCurrentWeather(lat, lon)
                val current = response.currentWeather
                Weather(
                    temperatureCelsius = current.temperature,
                    weatherCode = current.weatherCode,
                    description = WeatherCodeMapper.describe(current.weatherCode),
                    isRainy = WeatherCodeMapper.isRainy(current.weatherCode),
                    windSpeedKmh = current.windSpeed
                )
            }
        }

    override suspend fun reverseGeocode(lat: Double, lon: Double): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = geocodingApi.reverseGeocode(lat, lon)
                response.displayName
            }
        }
}
