package com.jeet.androidreminderapp.domain.use_case

import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import javax.inject.Inject

class UpsertTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Long> {
        if (task.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        if (task.latitude !in -90.0..90.0 || task.longitude !in -180.0..180.0) {
            return Result.failure(IllegalArgumentException("Invalid coordinates"))
        }
        if (task.radiusMeters <= 0f) {
            return Result.failure(IllegalArgumentException("Radius must be positive"))
        }
        return runCatching { repository.upsertTask(task) }
    }
}