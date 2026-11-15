package com.bloodtransport.iot.ui.screen

import com.bloodtransport.iot.data.model.CargoType
import com.bloodtransport.iot.data.model.SystemAlertState
import com.bloodtransport.iot.data.model.TelemetryLog
import com.bloodtransport.iot.data.repository.ConnectionStatus

/**
 * Historical statistics accumulated over the active transport trip.
 */
data class SessionStats(
    val maxTemperature: Double = Double.NEGATIVE_INFINITY,
    val minTemperature: Double = Double.POSITIVE_INFINITY,
    val tamperCount: Int = 0,
    val totalReadingsCount: Int = 0,
    val tripStartTime: String = ""
) {
    val displayMaxTemp: String
        get() = if (maxTemperature == Double.NEGATIVE_INFINITY) "--" else "%.1f°C".format(maxTemperature)

    val displayMinTemp: String
        get() = if (minTemperature == Double.POSITIVE_INFINITY) "--" else "%.1f°C".format(minTemperature)
}

/**
 * Immutable UI State representing the entire Blood Transport IoT Dashboard.
 */
data class DashboardUiState(
    val latestReading: TelemetryLog? = null,
    val recentReadings: List<TelemetryLog> = emptyList(), // Rolling buffer: last 5 readings
    val cargoType: CargoType = CargoType.WHOLE_BLOOD,
    val alertState: SystemAlertState = SystemAlertState.NORMAL,
    val sessionStats: SessionStats = SessionStats(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.SIMULATED,

    // Chain of custody modal state
    val isPinModalVisible: Boolean = false,
    val pinInput: String = "",
    val pinError: String? = null,

    // Completed Trip Summary screen state
    val isSummaryVisible: Boolean = false,

    // Cargo selector dropdown state
    val isCargoDropdownExpanded: Boolean = false,

    // Event counter to trigger heavy vibration on alert transitions
    val hapticAlertToken: Long = 0L
)
