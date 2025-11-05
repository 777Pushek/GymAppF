package com.example.gymappfrontendui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.repository.BodyMeasurementRepository
import com.example.gymappfrontendui.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BodyMeasurementsState(
    val date: String = "",
    val isoDate: String = "",
    val measurementId: Int = 0,
    val weight: String = "",
    val waist: String = "",
    val forearm: String = "",
    val chest: String = "",
    val calf: String = "",
    val thigh: String = "",
    val arm: String = "",
    val hips: String = "",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class BodyMeasurementsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = BodyMeasurementRepository(app.applicationContext)
    private val userRepository = UserRepository(app.applicationContext)

    private val _state = MutableStateFlow(BodyMeasurementsState())
    val state: StateFlow<BodyMeasurementsState> = _state.asStateFlow()

    private var currentUserId: Int? = null
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        viewModelScope.launch {
            currentUserId = userRepository.getLoggedInUserId() ?: userRepository.getGuestUserId()
            if (currentUserId == null) {
                _state.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            loadMeasurementsForDate(LocalDate.now())
        }
    }

    private fun loadMeasurementsForDate(date: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, saveSuccess = false) }
            val isoDateStr = date.format(isoDateFormatter)
            val displayDateStr = date.format(displayDateFormatter)

            repository.getAvailableBodyMeasurements()
                .map { measurements ->
                    measurements.find { it.date == isoDateStr && it.userId == currentUserId }
                }
                .collect { measurement ->
                    if (measurement != null) {
                        _state.update {
                            it.copy(
                                date = displayDateStr,
                                isoDate = isoDateStr,
                                measurementId = measurement.bodyMeasurementId,
                                weight = measurement.weight?.toString() ?: "",
                                waist = measurement.waist?.toString() ?: "",
                                forearm = measurement.forearm?.toString() ?: "",
                                chest = measurement.chest?.toString() ?: "",
                                calf = measurement.calf?.toString() ?: "",
                                thigh = measurement.thigh?.toString() ?: "",
                                arm = measurement.arm?.toString() ?: "",
                                hips = measurement.hips?.toString() ?: "",
                                isLoading = false
                            )
                        }
                    } else {
                        _state.update {
                            BodyMeasurementsState(
                                date = displayDateStr,
                                isoDate = isoDateStr,
                                measurementId = 0
                            ).copy(isLoading = false)
                        }
                    }
                }
        }
    }

    fun updateWeight(value: String) { _state.update { it.copy(weight = value, saveSuccess = false) } }
    fun updateWaist(value: String) { _state.update { it.copy(waist = value, saveSuccess = false) } }
    fun updateForearm(value: String) { _state.update { it.copy(forearm = value, saveSuccess = false) } }
    fun updateChest(value: String) { _state.update { it.copy(chest = value, saveSuccess = false) } }
    fun updateCalf(value: String) { _state.update { it.copy(calf = value, saveSuccess = false) } }
    fun updateThigh(value: String) { _state.update { it.copy(thigh = value, saveSuccess = false) } }
    fun updateArm(value: String) { _state.update { it.copy(arm = value, saveSuccess = false) } }
    fun updateHips(value: String) { _state.update { it.copy(hips = value, saveSuccess = false) } }

    fun goToNextDay() {
        changeDate(1)
    }

    fun goToPreviousDay() {
        changeDate(-1)
    }
    fun selectDate(selectedDate: LocalDate) {
        viewModelScope.launch {
            if (selectedDate.isAfter(LocalDate.now())) {
                _state.update { it.copy(error = "Cannot select future dates") }
                return@launch
            }
            loadMeasurementsForDate(selectedDate)
        }
    }
    private fun changeDate(daysToAdd: Long) {
        viewModelScope.launch {
            try {
                val currentDate = LocalDate.parse(_state.value.isoDate, isoDateFormatter)
                val newDate = currentDate.plusDays(daysToAdd)
                if (newDate.isAfter(LocalDate.now())) {
                    _state.update { it.copy(error = "Cannot select future dates") }
                    return@launch
                }
                loadMeasurementsForDate(newDate)

            } catch (e: Exception) {
                Log.e("BodyMeasurementsVM", "Error changing date", e)
                _state.update { it.copy(error = "Error changing date.") }
            }
        }
    }
    fun saveMeasurements() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentUserId == null) {
                _state.update { it.copy(error = "Cannot save: User ID not found.") }
                return@launch
            }
            val isAnyFieldSet = listOf(
                currentState.weight, currentState.waist, currentState.forearm,
                currentState.chest, currentState.calf, currentState.thigh,
                currentState.arm, currentState.hips
            ).any { it.isNotBlank() }

            if (!isAnyFieldSet) {
                _state.update { it.copy(error = "Enter at least one measurement to save.") }
                return@launch
            }


            _state.update { it.copy(isLoading = true, error = null) }

            val measurement = BodyMeasurement(
                bodyMeasurementId = currentState.measurementId,
                userId = currentUserId,
                date = currentState.isoDate,
                weight = currentState.weight.toFloatOrNull(),
                waist = currentState.waist.toFloatOrNull(),
                forearm = currentState.forearm.toFloatOrNull(),
                chest = currentState.chest.toFloatOrNull(),
                calf = currentState.calf.toFloatOrNull(),
                thigh = currentState.thigh.toFloatOrNull(),
                arm = currentState.arm.toFloatOrNull(),
                hips = currentState.hips.toFloatOrNull()
            )

            try {
                if (currentState.measurementId == 0) {
                    repository.insertBodyMeasurement(measurement)
                    Log.d("BodyMeasurementsVM", "Inserted new measurement")
                } else {
                    repository.updateBodyMeasurement(measurement)
                    Log.d("BodyMeasurementsVM", "Updated measurement ID: ${currentState.measurementId}")
                }
                _state.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                Log.e("BodyMeasurementsVM", "Error saving measurement", e)
                _state.update { it.copy(isLoading = false, error = "Failed to save measurements.") }
            }
        }
    }
}