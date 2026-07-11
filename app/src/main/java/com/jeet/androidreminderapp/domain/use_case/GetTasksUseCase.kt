package com.jeet.androidreminderapp.domain.use_case

import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = repository.observeTasks()
}
