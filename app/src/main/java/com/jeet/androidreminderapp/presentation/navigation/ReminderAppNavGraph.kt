package com.jeet.androidreminderapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeet.androidreminderapp.presentation.addtask.AddEditTaskScreen
import com.jeet.androidreminderapp.presentation.tasklist.TaskListScreen

@Composable
fun ReminderAppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.TASK_LIST) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onAddTask = { navController.navigate(Routes.addTask()) },
                onEditTask = { id -> navController.navigate(Routes.editTask(id)) }
            )
        }
        composable(
            route = "${Routes.ADD_EDIT_TASK}?${Routes.TASK_ID_ARG}={${Routes.TASK_ID_ARG}}",
            arguments = listOf(
                navArgument(Routes.TASK_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddEditTaskScreen(onDone = { navController.popBackStack() })
        }
    }
}

object Routes {
    const val TASK_LIST = "task_list"
    const val ADD_EDIT_TASK = "add_edit_task"
    const val TASK_ID_ARG = "taskId"

    fun addTask() = ADD_EDIT_TASK
    fun editTask(taskId: Long) = "$ADD_EDIT_TASK?$TASK_ID_ARG=$taskId"
}
