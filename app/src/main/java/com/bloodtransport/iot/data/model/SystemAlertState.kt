package com.bloodtransport.iot.data.model

/**
 * Three operational states evaluated from telemetry:
 * 1. NORMAL: Temperature is within the selected threshold, and is_tampered is false.
 * 2. TEMPERATURE_ALERT: Temperature is out of bounds (below minimum or above maximum).
 * 3. TAMPER_ALERT: is_tampered is true (critical integrity breach).
 */
enum class SystemAlertState {
    NORMAL,
    TEMPERATURE_ALERT,
    TAMPER_ALERT;

    val isAlert: Boolean
        get() = this != NORMAL

    val isCriticalTamper: Boolean
        get() = this == TAMPER_ALERT
}
