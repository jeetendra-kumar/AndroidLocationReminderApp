package com.jeet.androidreminderapp.presentation.addtask

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeet.androidreminderapp.domain.model.TriggerType
import com.jeet.androidreminderapp.domain.model.Weather
import com.jeet.androidreminderapp.presentation.mappicker.MapPickerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onDone: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    if (state.isMapPickerOpen) {
        MapPickerScreen(
            initialLat = state.latitude,
            initialLon = state.longitude,
            initialRadius = state.radiusMeters,
            onConfirm = { lat, lon, radius, name ->
                viewModel.onRadiusChange(radius)
                viewModel.onLocationPicked(lat, lon, name)
            },
            onDismiss = viewModel::closeMapPicker
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit reminder" else "New reminder") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("e.g. Buy groceries") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            LocationSection(
                locationName = state.locationName,
                latitude = state.latitude,
                longitude = state.longitude,
                radiusMeters = state.radiusMeters,
                onPickLocation = viewModel::openMapPicker
            )

            TriggerTypeSection(
                selected = state.triggerType,
                onSelect = viewModel::onTriggerTypeChange
            )

            FrequencySection(
                minutes = state.checkFrequencyMinutes,
                onChange = viewModel::onFrequencyChange
            )

            WeatherSection(
                enabled = state.notifyOnBadWeather,
                onToggle = viewModel::onWeatherToggle,
                isLoading = state.isLoadingWeatherPreview,
                preview = state?.weatherPreview
            )

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEditing) "Save changes" else "Create reminder")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LocationSection(
    locationName: String,
    latitude: Double?,
    longitude: Double?,
    radiusMeters: Float,
    onPickLocation: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.06f
            )
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Location", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            if (latitude != null && longitude != null) {
                Text(
                    locationName.ifBlank { "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}" },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Radius: ${radiusMeters.toInt()} m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "No location selected yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onPickLocation, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (latitude == null) "Pick on map" else "Change location")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriggerTypeSection(selected: TriggerType, onSelect: (TriggerType) -> Unit) {
    Column {
        Text("Notify me on", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            val options = listOf(
                TriggerType.ON_ARRIVAL to "Arrival",
                TriggerType.ON_DEPARTURE to "Departure",
                TriggerType.ON_DWELL to "Staying a while"
            )
            options.forEach { (type, label) ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    label = { Text(label) })
            }
        }
    }
}

@Composable
private fun FrequencySection(minutes: Int, onChange: (Int) -> Unit) {
    Column {
        Text(
            "Background check frequency: $minutes min",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "How often we check your location in the background between geofence events. " +
                    "Smart reminders will speed this up if you're often nearby, and slow it down to save battery otherwise.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = minutes.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 5f..60f,
            steps = 10
        )
    }
}

@Composable
private fun WeatherSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isLoading: Boolean,
    preview: Weather?
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Weather-aware alert", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Add a heads-up if rain is expected when you arrive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            if (enabled) {
                Spacer(Modifier.height(8.dp))
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else -> Text(
                        "Currently: ${preview?.description}, ${preview?.temperatureCelsius}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
