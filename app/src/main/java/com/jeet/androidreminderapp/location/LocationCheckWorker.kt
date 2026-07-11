package com.jeet.androidreminderapp.location

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jeet.androidreminderapp.domain.model.TriggerType
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import com.jeet.androidreminderapp.domain.use_case.EvaluateSmartReminderUseCase
import com.jeet.androidreminderapp.notification.ReminderNotificationManager
import com.jeet.androidreminderapp.util.LocationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class LocationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationService: LocationService,
    private val taskRepository: TaskRepository,
    private val notificationManager: ReminderNotificationManager,
    private val evaluateSmartReminder: EvaluateSmartReminderUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!locationService.hasLocationPermission()) return Result.success()

        val currentLocation = locationService.getCurrentLocation() ?: return Result.retry()
        val activeTasks = taskRepository.observeActiveTasks()

        // Snapshot the active tasks once rather than collecting the Flow indefinitely
        // inside a one-shot worker
        val tasks = runCatching {
            activeTasks.first()
        }.getOrDefault(emptyList())

        val now = System.currentTimeMillis()

        for (task in tasks) {
            val decision = evaluateSmartReminder(task, now)
            if (!decision.shouldCheckNow) continue

            val distance = LocationUtils.distanceMeters(
                currentLocation.latitude, currentLocation.longitude, task.latitude, task.longitude
            )
            val isInside = distance <= task.radiusMeters

            val shouldNotify = when (task.triggerType) {
                TriggerType.ON_ARRIVAL -> isInside
                TriggerType.ON_DEPARTURE -> !isInside && task.lastTriggeredAt == null
                TriggerType.ON_DWELL -> isInside
            }

            if (shouldNotify) {
                var weatherNote: String? = null
                if (task.notifyOnBadWeather && task.triggerType == TriggerType.ON_ARRIVAL) {
                    taskRepository.getCurrentWeather(task.latitude, task.longitude)
                        .onSuccess { weather ->
                            if (weather.isRainy) {
                                weatherNote = "☔ ${weather.description}, ${weather.temperatureCelsius}°C — you might want an umbrella."
                            }
                        }
                }
                notificationManager.showArrivalReminder(task, weatherNote)
                taskRepository.markTriggered(task.id, now)
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "periodic_location_check"
    }
}
