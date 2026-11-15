package com.bloodtransport.iot.data.model

/**
 * Biological cargo classification for medical transport.
 * Defines critical temperature threshold windows:
 * - Whole Blood: 2.0°C to 6.0°C
 * - Platelets: 20.0°C to 24.0°C
 */
enum class CargoType(
    val displayName: String,
    val minThreshold: Double,
    val maxThreshold: Double
) {
    WHOLE_BLOOD(
        displayName = "Whole Blood",
        minThreshold = 2.0,
        maxThreshold = 6.0
    ),
    PLATELETS(
        displayName = "Platelets",
        minThreshold = 20.0,
        maxThreshold = 24.0
    );

    /**
     * Verifies if a given temperature falls within the acceptable cold chain window.
     */
    fun isSafe(temperature: Double): Boolean {
        return temperature in minThreshold..maxThreshold
    }

    /**
     * Returns a clean range string for display (e.g. "2.0°C – 6.0°C").
     */
    val thresholdDisplay: String
        get() = "%.1f°C – %.1f°C".format(minThreshold, maxThreshold)
}
