package com.bloodtransport.iot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodtransport.iot.data.model.CargoType
import com.bloodtransport.iot.data.model.SystemAlertState
import com.bloodtransport.iot.data.model.TelemetryLog
import com.bloodtransport.iot.data.repository.TelemetryRepository
import com.bloodtransport.iot.ui.screen.DashboardUiState
import com.bloodtransport.iot.ui.screen.SessionStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ViewModel managing real-time IoT state, alert classification,
 * rolling telemetry buffer, and chain of custody completion.
 */
class DashboardViewModel(
    private val repository: TelemetryRepository
) : ViewModel() {

    companion object {
        const val VALID_CUSTODY_PIN = "1234"
        const val MAX_ROLLING_READINGS = 5
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTelemetry()
        observeConnectionStatus()
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            repository.telemetryStream.collect { newReading ->
                processIncomingReading(newReading)
            }
        }
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            repository.connectionState.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }
    }

    /**
     * Ingests a new telemetry record, updates rolling 5-item buffer,
     * updates session statistics, and re-evaluates system alert state.
     */
    private fun processIncomingReading(reading: TelemetryLog) {
        _uiState.update { current ->
            // 1. Evaluate three alert states
            val newAlertState = evaluateAlertState(reading, current.cargoType)

            // 2. Trigger heavy haptic alert if entering or persisting in an alert state
            val alertToken = if (newAlertState.isAlert && newAlertState != current.alertState) {
                System.currentTimeMillis()
            } else if (newAlertState.isAlert && current.alertState.isAlert) {
                // Persistent alert reminder
                System.currentTimeMillis()
            } else {
                current.hapticAlertToken
            }

            // 3. Maintain rolling list of the last 5 readings
            val updatedRolling = (listOf(reading) + current.recentReadings).take(MAX_ROLLING_READINGS)

            // 4. Update session statistics
            val updatedStats = current.sessionStats.copy(
                maxTemperature = maxOf(
                    if (current.sessionStats.maxTemperature == Double.NEGATIVE_INFINITY) reading.temperature
                    else current.sessionStats.maxTemperature,
                    reading.temperature
                ),
                minTemperature = minOf(
                    if (current.sessionStats.minTemperature == Double.POSITIVE_INFINITY) reading.temperature
                    else current.sessionStats.minTemperature,
                    reading.temperature
                ),
                tamperCount = current.sessionStats.tamperCount + (if (reading.isTampered) 1 else 0),
                totalReadingsCount = current.sessionStats.totalReadingsCount + 1,
                tripStartTime = current.sessionStats.tripStartTime.ifBlank { reading.formattedTime }
            )

            current.copy(
                latestReading = reading,
                recentReadings = updatedRolling,
                alertState = newAlertState,
                sessionStats = updatedStats,
                hapticAlertToken = alertToken
            )
        }
    }

    /**
     * Determines system alert state based on business requirements:
     * - TAMPER ALERT: is_tampered is true (highest priority).
     * - TEMPERATURE ALERT: Temperature is out of bounds for the active CargoType.
     * - NORMAL: Temperature is within the selected threshold and is_tampered is false.
     */
    private fun evaluateAlertState(reading: TelemetryLog, cargoType: CargoType): SystemAlertState {
        return when {
            reading.isTampered -> SystemAlertState.TAMPER_ALERT
            !cargoType.isSafe(reading.temperature) -> SystemAlertState.TEMPERATURE_ALERT
            else -> SystemAlertState.NORMAL
        }
    }

    /**
     * Selects a new Cargo Type (Whole Blood [2-6°C] or Platelets [20-24°C])
     * and instantly re-evaluates the alert state against the latest reading.
     */
    fun selectCargoType(type: CargoType) {
        _uiState.update { current ->
            val updatedAlert = current.latestReading?.let {
                evaluateAlertState(it, type)
            } ?: SystemAlertState.NORMAL

            val token = if (updatedAlert.isAlert && updatedAlert != current.alertState) {
                System.currentTimeMillis()
            } else {
                current.hapticAlertToken
            }

            current.copy(
                cargoType = type,
                alertState = updatedAlert,
                isCargoDropdownExpanded = false,
                hapticAlertToken = token
            )
        }
    }

    fun toggleCargoDropdown(expanded: Boolean) {
        _uiState.update { it.copy(isCargoDropdownExpanded = expanded) }
    }

    // =========================================================================
    // Chain of Custody & PIN Handling
    // =========================================================================

    fun openPinModal() {
        _uiState.update {
            it.copy(
                isPinModalVisible = true,
                pinInput = "",
                pinError = null
            )
        }
    }

    fun dismissPinModal() {
        _uiState.update {
            it.copy(
                isPinModalVisible = false,
                pinInput = "",
                pinError = null
            )
        }
    }

    fun onPinDigitEntered(digit: Char) {
        val current = _uiState.value.pinInput
        if (current.length < 4 && digit.isDigit()) {
            val updated = current + digit
            _uiState.update { it.copy(pinInput = updated, pinError = null) }
            if (updated.length == 4) {
                verifyPin(updated)
            }
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.pinInput
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(pinInput = current.dropLast(1), pinError = null) }
        }
    }

    private fun verifyPin(pin: String) {
        if (pin == VALID_CUSTODY_PIN) {
            _uiState.update {
                it.copy(
                    isPinModalVisible = false,
                    isSummaryVisible = true,
                    pinInput = "",
                    pinError = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    pinInput = "",
                    pinError = "Incorrect PIN. Default custody PIN is 1234."
                )
            }
        }
    }

    fun dismissSummary() {
        _uiState.update { it.copy(isSummaryVisible = false) }
    }

    fun resetTripSession() {
        _uiState.update { current ->
            current.copy(
                isSummaryVisible = false,
                sessionStats = SessionStats(),
                recentReadings = emptyList()
            )
        }
    }

    // =========================================================================
    // Simulation & Testing Triggers
    // =========================================================================

    fun triggerSimulatedReading(temp: Double, tampered: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.latestReading
            val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val reading = TelemetryLog(
                id = UUID.randomUUID().toString(),
                tripId = current?.tripId ?: "TRIP-8842-US",
                temperature = temp,
                batteryPercentage = current?.batteryPercentage ?: 85,
                signalStrength = current?.signalStrength ?: 90,
                timestamp = timeFormat.format(Date()),
                isTampered = tampered
            )
            repository.injectSimulatedReading(reading)
        }
    }
}
