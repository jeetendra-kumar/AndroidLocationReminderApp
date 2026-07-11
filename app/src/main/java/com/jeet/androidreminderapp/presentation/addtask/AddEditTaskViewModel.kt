package com.jeet.androidreminderapp.presentation.addtask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeet.androidreminderapp.domain.model.Task
import com.jeet.androidreminderapp.domain.model.TriggerType
import com.jeet.androidreminderapp.domain.model.Weather
import com.jeet.androidreminderapp.domain.repository.TaskRepository
import com.jeet.androidreminderapp.domain.use_case.UpsertTaskUseCase
import com.jeet.androidreminderapp.location.GeofenceHelper
import com.jeet.androidreminderapp.location.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val geofenceHelper: GeofenceHelper,
    private val locationService: LocationService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init {
        val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L
        if (taskId > 0) loadTask(taskId)
        else useCurrentLocationAsDefault()
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            taskRepository.getTaskById(id)?.let { task ->
                _uiState.update {
                    it.copy(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        latitude = task.latitude,
                        longitude = task.longitude,
                        locationName = task.locationName,
                        radiusMeters = task.radiusMeters,
                        triggerType = task.triggerType,
                        checkFrequencyMinutes = task.checkFrequencyMinutes,
                        notifyOnBadWeather = task.notifyOnBadWeather,
                        isEditing = true
                    )
                }
            }
        }
    }

    private fun useCurrentLocationAsDefault() {
        viewModelScope.launch {
            val loc = runCatching { locationService.getCurrentLocation() }.getOrNull()
            if (loc != null && _uiState.value.latitude == null) {
                _uiState.update { it.copy(latitude = loc.latitude, longitude = loc.longitude) }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onRadiusChange(value: Float) = _uiState.update { it.copy(radiusMeters = value) }
    fun onTriggerTypeChange(value: TriggerType) = _uiState.update { it.copy(triggerType = value) }
    fun onFrequencyChange(minutes: Int) =
        _uiState.update { it.copy(checkFrequencyMinutes = minutes) }

    fun onWeatherToggle(enabled: Boolean) {
        _uiState.update { it.copy(notifyOnBadWeather = enabled) }
        if (enabled) previewWeather()
    }

    fun openMapPicker() = _uiState.update { it.copy(isMapPickerOpen = true) }
    fun closeMapPicker() = _uiState.update { it.copy(isMapPickerOpen = false) }

    fun onLocationPicked(lat: Double, lon: Double, name: String) {
        _uiState.update {
            it.copy(latitude = lat, longitude = lon, locationName = name, isMapPickerOpen = false)
        }
        if (name.isBlank()) resolveLocationName(lat, lon)
        if (_uiState.value.notifyOnBadWeather) previewWeather()
    }

    private fun resolveLocationName(lat: Double, lon: Double) {
        viewModelScope.launch {
            taskRepository.reverseGeocode(lat, lon).onSuccess { address ->
                // Keep it short: first couple of comma-separated segments read best in a list row.
                val shortName = address.split(",").take(2).joinToString(",").trim()
                _uiState.update { it.copy(locationName = shortName) }
            }
        }
    }

    private fun previewWeather() {
        val state = _uiState.value
        val lat = state.latitude ?: return
        val lon = state.longitude ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWeatherPreview = true) }
            taskRepository.getCurrentWeather(lat, lon)
                .onSuccess { weather -> _uiState.update { it.copy(weatherPreview = weather) } }
            _uiState.update { it.copy(isLoadingWeatherPreview = false) }
        }
    }

    fun save() {
        val state = _uiState.value
        val lat = state.latitude
        val lon = state.longitude
        if (lat == null || lon == null) {
            _uiState.update { it.copy(errorMessage = "Please pick a location on the map") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val task = Task(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim(),
                latitude = lat,
                longitude = lon,
                locationName = state.locationName,
                radiusMeters = state.radiusMeters,
                triggerType = state.triggerType,
                checkFrequencyMinutes = state.checkFrequencyMinutes,
                notifyOnBadWeather = state.notifyOnBadWeather
            )
            upsertTaskUseCase(task).fold(
                onSuccess = { newId ->
                    val savedTask = task.copy(id = if (task.id == 0L) newId else task.id)
                    geofenceHelper.addGeofence(savedTask)
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
            )
        }
    }
}

data class AddEditTaskUiState(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val radiusMeters: Float = 200f,
    val triggerType: TriggerType = TriggerType.ON_ARRIVAL,
    val checkFrequencyMinutes: Int = 15,
    val notifyOnBadWeather: Boolean = false,
    val isMapPickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val isLoadingWeatherPreview: Boolean = false,
    val weatherPreview: Weather? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false
) {
    val isLocationSet: Boolean get() = latitude != null && longitude != null
    val canSave: Boolean get() = title.isNotBlank() && isLocationSet
}
