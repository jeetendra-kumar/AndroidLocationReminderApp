package com.jeet.androidreminderapp.domain.use_case

import com.jeet.androidreminderapp.domain.repository.TaskRepository
import javax.inject.Inject

class ToggleTaskCompletedUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: Long, completed: Boolean) =
        repository.setTaskCompleted(id, completed)
}