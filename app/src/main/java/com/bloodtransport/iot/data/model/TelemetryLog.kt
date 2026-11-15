package com.bloodtransport.iot.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Real-time IoT Telemetry record payload streamed from the PostgreSQL table "telemetry_logs".
 *
 * Schema:
 * - id: String / UUID
 * - trip_id: String
 * - temperature: Double (Celsius)
 * - battery_percentage: Int (0 - 100)
 * - signal_strength: Int (0 - 100 or dBm)
 * - timestamp: String (ISO-8601 or formatted)
 * - is_tampered: Boolean
 */
@Serializable
data class TelemetryLog(
    @SerialName("id")
    val id: String = "",

    @SerialName("trip_id")
    val tripId: String = "",

    @SerialName("temperature")
    val temperature: Double = 0.0,

    @SerialName("battery_percentage")
    val batteryPercentage: Int = 100,

    @SerialName("signal_strength")
    val signalStrength: Int = 100,

    @SerialName("timestamp")
    val timestamp: String = "",

    @SerialName("is_tampered")
    val isTampered: Boolean = false
) {
    /**
     * Formats temperature to single decimal precision (e.g. "4.2°C").
     */
    val formattedTemperature: String
        get() = "%.1f°C".format(temperature)

    /**
     * Extracts readable time representation from timestamp string.
     */
    val formattedTime: String
        get() = try {
            if (timestamp.contains("T")) {
                // ISO timestamp like 2026-11-15T14:32:00Z -> 14:32:00
                timestamp.substringAfter("T").substringBefore(".").substringBefore("Z")
            } else if (timestamp.contains(" ")) {
                // SQL timestamp like 2026-11-15 14:32:00 -> 14:32:00
                timestamp.substringAfter(" ")
            } else {
                timestamp
            }
        } catch (_: Exception) {
            timestamp
        }
}
