package com.jeet.androidreminderapp.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: LocationCheckScheduler
    @Inject lateinit var geofenceHelper: GeofenceHelper
    @Inject lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        scheduler.schedule()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeTasks = taskRepository.observeActiveTasks().first()
                activeTasks.forEach { geofenceHelper.addGeofence(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
