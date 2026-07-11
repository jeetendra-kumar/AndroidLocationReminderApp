package com.jeet.androidreminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jeet.androidreminderapp.presentation.navigation.ReminderAppNavGraph
import com.jeet.androidreminderapp.presentation.theme.AndroidReminderAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidReminderAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReminderAppNavGraph()
                }
            }
        }
    }
}