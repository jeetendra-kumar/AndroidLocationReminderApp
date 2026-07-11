package com.jeet.androidreminderapp.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.jeet.androidreminderapp.domain.model.TriggerType
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import com.jeet.androidreminderapp.notification.ReminderNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var notificationManager: ReminderNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e(TAG, "Geofencing error code: ${event.errorCode}")
            return
        }

        val transition = event.geofenceTransition
        val triggeringIds = event.triggeringGeofences?.mapNotNull { it.requestId } ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                triggeringIds.forEach { requestId ->
                    val taskId = requestId.removePrefix("task_").toLongOrNull() ?: return@forEach
                    val task = taskRepository.getTaskById(taskId) ?: return@forEach
                    if (!task.isActive || task.isCompleted) return@forEach

                    when (transition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER ->
                            if (task.triggerType == TriggerType.ON_ARRIVAL) {
                                notificationManager.showArrivalReminder(task)
                                taskRepository.markTriggered(task.id, System.currentTimeMillis())
                            }
                        Geofence.GEOFENCE_TRANSITION_EXIT ->
                            if (task.triggerType == TriggerType.ON_DEPARTURE) {
                                notificationManager.showDepartureReminder(task)
                                taskRepository.markTriggered(task.id, System.currentTimeMillis())
                            }
                        Geofence.GEOFENCE_TRANSITION_DWELL ->
                            if (task.triggerType == TriggerType.ON_DWELL) {
                                notificationManager.showDwellReminder(task)
                                taskRepository.markTriggered(task.id, System.currentTimeMillis())
                            }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
