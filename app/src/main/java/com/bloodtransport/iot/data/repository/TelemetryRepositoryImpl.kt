package com.bloodtransport.iot.data.repository

import android.util.Log
import com.bloodtransport.iot.data.config.SupabaseConfig
import com.bloodtransport.iot.data.model.TelemetryLog
import com.bloodtransport.iot.data.remote.SupabaseProvider

// CORRECTED SUPABASE IMPORTS (Removed ".tennert")
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime

// STANDARD KOTLIN & COROUTINE IMPORTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * Concrete implementation of [TelemetryRepository].
 * Subscribes to the PostgreSQL "telemetry_logs" table via Supabase Realtime WebSockets.
 * If credentials are not yet set or simulation is active, seamlessly emits simulated sensor data.
 */
class TelemetryRepositoryImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : TelemetryRepository {

    private val tag = "TelemetryRepository"

    private val _telemetryStream = MutableSharedFlow<TelemetryLog>(replay = 1)
    override val telemetryStream: Flow<TelemetryLog> = _telemetryStream.asSharedFlow()

    private val _connectionState = MutableStateFlow(
        if (SupabaseConfig.isConfigured) ConnectionStatus.CONNECTING else ConnectionStatus.SIMULATED
    )
    override val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()

    private var isSimulating = !SupabaseConfig.isConfigured
    private var simulationJob = scope.launch { }

    init {
        if (SupabaseConfig.isConfigured) {
            connectToSupabaseRealtime()
        } else {
            Log.w(tag, "Supabase credentials placeholder detected. Running in SIMULATED mode.")
            startSimulation()
        }
    }

    /**
     * Connects to Supabase Realtime WebSockets and listens to INSERT / UPDATE operations
     * on table "telemetry_logs".
     */
    private fun connectToSupabaseRealtime() {
        scope.launch {
            try {
                _connectionState.value = ConnectionStatus.CONNECTING
                val client = SupabaseProvider.client

                // Fetch initial readings via Postgrest REST
                fetchInitialReadings()

                // Setup Realtime WebSocket Channel
                val channel = client.realtime.channel(SupabaseConfig.REALTIME_CHANNEL_NAME)

                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = SupabaseConfig.SCHEMA_PUBLIC) {
                    table = SupabaseConfig.TABLE_TELEMETRY_LOGS
                }

                // Subscribe to channel
                channel.subscribe()
                _connectionState.value = ConnectionStatus.CONNECTED
                Log.i(tag, "Subscribed to Supabase Realtime table: ${SupabaseConfig.TABLE_TELEMETRY_LOGS}")

                changeFlow.collect { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val record = action.decodeRecord<TelemetryLog>()
                            Log.d(tag, "Realtime INSERT received: $record")
                            _telemetryStream.emit(record)
                        }
                        is PostgresAction.Update -> {
                            val record = action.decodeRecord<TelemetryLog>()
                            Log.d(tag, "Realtime UPDATE received: $record")
                            _telemetryStream.emit(record)
                        }
                        else -> {
                            Log.d(tag, "Ignored Postgres Action: $action")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to connect to Supabase Realtime: ${e.message}", e)
                _connectionState.value = ConnectionStatus.DISCONNECTED
                // Fallback to simulation so the app is always observable
                if (isSimulating) {
                    startSimulation()
                }
            }
        }
    }

    override suspend fun fetchInitialReadings(limit: Long): Result<List<TelemetryLog>> {
        if (!SupabaseConfig.isConfigured) {
            return Result.success(emptyList())
        }
        return try {
            val client = SupabaseProvider.client
            val logs = client.from(SupabaseConfig.TABLE_TELEMETRY_LOGS)
                .select {
                    order(column = "timestamp", order = Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<TelemetryLog>()

            // Re-emit latest if found
            logs.firstOrNull()?.let { _telemetryStream.emit(it) }
            Result.success(logs)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching initial telemetry readings: ${e.message}")
            Result.failure(e)
        }
    }

    override fun setSimulationMode(enabled: Boolean) {
        isSimulating = enabled
        if (enabled) {
            _connectionState.value = ConnectionStatus.SIMULATED
            startSimulation()
        } else {
            simulationJob.cancel()
            if (SupabaseConfig.isConfigured) {
                connectToSupabaseRealtime()
            } else {
                _connectionState.value = ConnectionStatus.DISCONNECTED
            }
        }
    }

    override suspend fun injectSimulatedReading(reading: TelemetryLog) {
        _telemetryStream.emit(reading)
    }

    private fun startSimulation() {
        simulationJob.cancel()
        simulationJob = scope.launch {
            var battery = 96
            var currentTemp = 4.2
            val tripId = "TRIP-8842-US"

            while (isActive) {
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val nowStr = timeFormat.format(Date())

                // Slight realistic fluctuation (+/- 0.2°C)
                currentTemp = (currentTemp + (Random.nextDouble(-0.25, 0.25)))
                    .coerceIn(1.5, 7.5)

                val telemetry = TelemetryLog(
                    id = UUID.randomUUID().toString(),
                    tripId = tripId,
                    temperature = (currentTemp * 10.0).toInt() / 10.0,
                    batteryPercentage = battery,
                    signalStrength = Random.nextInt(75, 99),
                    timestamp = nowStr,
                    isTampered = false
                )

                _telemetryStream.emit(telemetry)

                // Battery slowly discharges every few steps
                if (battery > 15 && Random.nextInt(10) == 0) {
                    battery -= 1
                }

                delay(3000L) // 3-second heartbeat
            }
        }
    }
}
