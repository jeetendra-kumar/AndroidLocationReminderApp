package com.jeet.androidreminderapp.domain.use_case

import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) = repository.deleteTask(task)
}