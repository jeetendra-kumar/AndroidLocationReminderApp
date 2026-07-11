package com.jeet.androidreminderapp.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.use_case.DeleteTaskUseCase
import com.jeet.androidreminderapp.domain.use_case.GetTasksUseCase
import com.jeet.androidreminderapp.domain.use_case.ToggleTaskCompletedUseCase
import com.jeet.androidreminderapp.location.GeofenceHelper
import com.jeet.androidreminderapp.presentation.tasklist.TaskFilter
import com.jeet.androidreminderapp.presentation.tasklist.TaskListViewModel
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val tasksFlow = MutableStateFlow(
        listOf(
            Task(
                id = 1,
                title = "Active task",
                latitude = 0.0,
                longitude = 0.0,
                isCompleted = false
            ),
            Task(id = 2, title = "Done task", latitude = 0.0, longitude = 0.0, isCompleted = true)
        )
    )

    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val deleteTaskUseCase: DeleteTaskUseCase = mockk()
    private val toggleTaskCompletedUseCase: ToggleTaskCompletedUseCase = mockk()
    private val geofenceHelper: GeofenceHelper = mockk()

    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getTasksUseCase() } returns tasksFlow
        coJustRun { toggleTaskCompletedUseCase(any(), any()) }
        coJustRun { deleteTaskUseCase(any()) }
        every { geofenceHelper.removeGeofence(any()) } returns Unit
        viewModel = TaskListViewModel(getTasksUseCase, deleteTaskUseCase, toggleTaskCompletedUseCase, geofenceHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default filter shows only active tasks`() = runTest(dispatcher) {
        viewModel.uiState.test {
            dispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.filter).isEqualTo(TaskFilter.ACTIVE)
            assertThat(state.visibleTasks.map { it.id }).containsExactly(1L)
        }
    }

    @Test
    fun `switching filter to ALL shows every task`() = runTest(dispatcher) {
        viewModel.setFilter(TaskFilter.ALL)
        viewModel.uiState.test {
            dispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.visibleTasks.map { it.id }).containsExactly(1L, 2L)
        }
    }

    @Test
    fun `switching filter to COMPLETED shows only completed tasks`() = runTest(dispatcher) {
        viewModel.setFilter(TaskFilter.COMPLETED)
        viewModel.uiState.test {
            dispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.visibleTasks.map { it.id }).containsExactly(2L)
        }
    }
}