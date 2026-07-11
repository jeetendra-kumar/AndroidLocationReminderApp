package com.jeet.androidreminderapp.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.model.TriggerType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun keyFor(taskId: Long) = "task_$taskId"

    @SuppressLint("MissingPermission")
    suspend fun addGeofence(task: Task) {
        val transitionTypes = when (task.triggerType) {
            TriggerType.ON_ARRIVAL -> Geofence.GEOFENCE_TRANSITION_ENTER
            TriggerType.ON_DEPARTURE -> Geofence.GEOFENCE_TRANSITION_EXIT
            TriggerType.ON_DWELL -> Geofence.GEOFENCE_TRANSITION_DWELL
        }

        val geofence = Geofence.Builder()
            .setRequestId(keyFor(task.id))
            .setCircularRegion(task.latitude, task.longitude, task.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .apply { if (task.triggerType == TriggerType.ON_DWELL) setLoiteringDelay(5 * 60 * 1000) }
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        runCatching {
            geofencingClient.addGeofences(request, geofencePendingIntent).await()
        }
    }

    fun removeGeofence(taskId: Long) {
        geofencingClient.removeGeofences(listOf(keyFor(taskId)))
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.example.reminderapp.ACTION_GEOFENCE_EVENT"
    }
}
