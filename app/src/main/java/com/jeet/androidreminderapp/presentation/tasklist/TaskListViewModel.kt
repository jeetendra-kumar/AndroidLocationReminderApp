package com.jeet.androidreminderapp.presentation.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.use_case.DeleteTaskUseCase
import com.jeet.androidreminderapp.domain.use_case.GetTasksUseCase
import com.jeet.androidreminderapp.domain.use_case.ToggleTaskCompletedUseCase
import com.jeet.androidreminderapp.location.GeofenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletedUseCase: ToggleTaskCompletedUseCase,
    private val geofenceHelper: GeofenceHelper
) : ViewModel() {

    private val filter = MutableStateFlow(TaskFilter.ACTIVE)
    private val tasksFlow = getTasksUseCase()

    val uiState: StateFlow<TaskListUiState> = combine(tasksFlow, filter) { tasks, currentFilter ->
        TaskListUiState(tasks = tasks, filter = currentFilter, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState()
    )

    fun setFilter(newFilter: TaskFilter) {
        filter.value = newFilter
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            toggleTaskCompletedUseCase(task.id, !task.isCompleted)
            if (!task.isCompleted) geofenceHelper.removeGeofence(task.id)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            geofenceHelper.removeGeofence(task.id)
            deleteTaskUseCase(task)
        }
    }
}

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ACTIVE,
    val isLoading: Boolean = true
) {
    val visibleTasks: List<Task>
        get() = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
}

enum class TaskFilter { ALL, ACTIVE, COMPLETED }
