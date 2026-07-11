package com.jeet.androidreminderapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeet.androidreminderapp.data.local.dao.TaskDao
import com.jeet.androidreminderapp.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "reminder_app.db"
    }
}