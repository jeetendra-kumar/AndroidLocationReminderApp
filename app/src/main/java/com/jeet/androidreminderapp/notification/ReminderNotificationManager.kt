package com.jeet.androidreminderapp.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jeet.androidreminderapp.MainActivity
import com.jeet.androidreminderapp.R
import com.jeet.androidreminderapp.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "location_reminders_channel"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_desc)
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showArrivalReminder(task: Task, weatherNote: String? = null) {
        val body = buildString {
            append(task.description.ifBlank { "You've arrived near ${task.locationName.ifBlank { "your destination" }}." })
            if (weatherNote != null) append("\n\n$weatherNote")
        }
        show(task.id, "📍 ${task.title}", body)
    }

    @SuppressLint("MissingPermission")
    fun showDepartureReminder(task: Task) {
        show(task.id, "👋 ${task.title}", "You're leaving ${task.locationName.ifBlank { "the area" }}. ${task.description}")
    }

    @SuppressLint("MissingPermission")
    fun showDwellReminder(task: Task) {
        show(task.id, "⏱️ ${task.title}", "Still here? ${task.description}")
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun show(taskId: Long, title: String, body: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // Permission not granted — silently skip; UI should have prompted for it.
        }

        NotificationManagerCompat.from(context)
            .notify(taskId.toInt(), notification)
    }
}
