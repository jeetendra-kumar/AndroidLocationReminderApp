package com.jeet.androidreminderapp.presentation.mappicker

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLat: Double?,
    initialLon: Double?,
    initialRadius: Float,
    onConfirm: (lat: Double, lon: Double, radius: Float, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val startLat = initialLat ?: 28.66249
    val startLon = initialLon ?: 77.43777

    var pickedLocation by remember { mutableStateOf(LatLng(startLat, startLon)) }
    var radius by remember { mutableStateOf(initialRadius) }

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickedLocation, 15f)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Choose a location") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> pickedLocation = latLng }
                ) {
                    Marker(state = MarkerState(position = pickedLocation), title = "Reminder location")
                }
            }

            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Geofence radius: ${radius.toInt()} m",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 50f..2000f,
                        steps = 38
                    )
                    Text(
                        "Lat ${"%.5f".format(pickedLocation.latitude)}, Lon ${"%.5f".format(pickedLocation.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                pickedLocation.latitude,
                                pickedLocation.longitude,
                                radius,
                                "" // Reverse geocoding could populate this via the optional geocoding API
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirm location")
                    }
                }
            }
        }
    }
}
