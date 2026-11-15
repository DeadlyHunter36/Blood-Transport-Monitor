package com.bloodtransport.iot.data.repository

import com.bloodtransport.iot.data.model.TelemetryLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface defining operations for streaming and retrieving
 * IoT telemetry logs from Supabase Realtime and Postgrest.
 */
interface TelemetryRepository {

    /**
     * Continuous WebSocket stream of incoming telemetry logs.
     */
    val telemetryStream: Flow<TelemetryLog>

    /**
     * Observable state of connection status: CONNECTED, CONNECTING, DISCONNECTED, or SIMULATED.
     */
    val connectionState: StateFlow<ConnectionStatus>

    /**
     * Fetches the latest historical telemetry readings for the initial state.
     */
    suspend fun fetchInitialReadings(limit: Long = 5): Result<List<TelemetryLog>>

    /**
     * Toggles offline simulation mode for local testing and demonstration.
     */
    fun setSimulationMode(enabled: Boolean)

    /**
     * Injects a custom simulated telemetry reading (e.g. for testing alerts or tamper).
     */
    suspend fun injectSimulatedReading(reading: TelemetryLog)
}

enum class ConnectionStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    SIMULATED
}
