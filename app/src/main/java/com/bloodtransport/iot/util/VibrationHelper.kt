package com.bloodtransport.iot.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Utility helper for triggering high-intensity haptic vibration alerts
 * when out-of-bounds temperature or tamper breaches occur.
 */
object VibrationHelper {

    private const val TAG = "VibrationHelper"

    /**
     * Heavy alert vibration pattern:
     * High intensity burst followed by double-pulse impact.
     */
    fun vibrateHeavyAlert(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val defaultVibrator = vibratorManager?.defaultVibrator
                if (defaultVibrator?.hasVibrator() == true) {
                    val timings = longArrayOf(0, 400, 120, 400, 120, 600)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    val combined = CombinedVibration.createParallel(effect)
                    vibratorManager.vibrate(combined)
                    return
                }
            }

            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 400, 120, 400, 120, 600)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    val pattern = longArrayOf(0, 400, 120, 400, 120, 600)
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute vibration: ${e.message}", e)
        }
    }

    /**
     * Single subtle click feedback for buttons/keypad.
     */
    fun vibrateClick(context: Context) {
        try {
            @Suppress("DEPRECATION")
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed click vibration: ${e.message}")
        }
    }
}
