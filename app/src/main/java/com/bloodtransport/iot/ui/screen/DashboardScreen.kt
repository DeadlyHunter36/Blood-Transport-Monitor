package com.bloodtransport.iot.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodtransport.iot.data.model.CargoType
import com.bloodtransport.iot.data.model.SystemAlertState
import com.bloodtransport.iot.ui.components.BatteryIcon
import com.bloodtransport.iot.ui.components.PinModalDialog
import com.bloodtransport.iot.ui.components.SignalIcon
import com.bloodtransport.iot.ui.components.TamperBanner
import com.bloodtransport.iot.ui.components.TelemetryHistoryList
import com.bloodtransport.iot.ui.components.TripSummaryDialog
import com.bloodtransport.iot.ui.theme.AlertRedBackground
import com.bloodtransport.iot.ui.theme.AlertYellowBackground
import com.bloodtransport.iot.ui.theme.PureBlack
import com.bloodtransport.iot.ui.theme.PureWhite
import com.bloodtransport.iot.ui.viewmodel.DashboardViewModel
import com.bloodtransport.iot.util.VibrationHelper

/**
 * Main IoT Dashboard Screen for the Blood Transport Temperature Monitoring System.
 *
 * Implements MVVM pattern, high-contrast monochrome base, dynamic alert backgrounds,
 * massive real-time temperature display, chain of custody PIN authentication, and haptics.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observe alert token to trigger heavy haptic vibration on alert changes
    LaunchedEffect(uiState.hapticAlertToken) {
        if (uiState.hapticAlertToken > 0L) {
            VibrationHelper.vibrateHeavyAlert(context)
        }
    }

    // Dynamic background and text color based on evaluated SystemAlertState:
    // - NORMAL: Background Black, Text White
    // - TEMPERATURE ALERT: Background solid Red, Text White
    // - TAMPER ALERT: Background solid Yellow, Text Black
    val targetBackgroundColor = when (uiState.alertState) {
        SystemAlertState.NORMAL -> PureBlack
        SystemAlertState.TEMPERATURE_ALERT -> AlertRedBackground
        SystemAlertState.TAMPER_ALERT -> AlertYellowBackground
    }

    val contentColor = when (uiState.alertState) {
        SystemAlertState.NORMAL -> PureWhite
        SystemAlertState.TEMPERATURE_ALERT -> PureWhite
        SystemAlertState.TAMPER_ALERT -> PureBlack
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 350),
        label = "DashboardBackgroundAnimation"
    )

    var showTestingDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBackgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 1. TOP BAR: Trip ID, Battery, Signal
                DashboardTopBar(
                    tripId = uiState.latestReading?.tripId ?: "TRIP-8842-US",
                    batteryPercentage = uiState.latestReading?.batteryPercentage ?: 85,
                    signalStrength = uiState.latestReading?.signalStrength ?: 92,
                    tint = contentColor,
                    onToggleTestPanel = { showTestingDrawer = !showTestingDrawer }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tamper Alert Banner (visible only in TAMPER_ALERT)
                TamperBanner(
                    visible = uiState.alertState == SystemAlertState.TAMPER_ALERT,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 2. SETTINGS DROPDOWN: Cargo Type Selection
                CargoTypeDropdown(
                    selectedType = uiState.cargoType,
                    isExpanded = uiState.isCargoDropdownExpanded,
                    tint = contentColor,
                    onExpandChange = { viewModel.toggleCargoDropdown(it) },
                    onTypeSelected = { viewModel.selectCargoType(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. CENTER DISPLAY: Massive Real-Time Temperature
            RealtimeTemperatureDisplay(
                temperature = uiState.latestReading?.temperature,
                alertState = uiState.alertState,
                cargoType = uiState.cargoType,
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ROLLING LIST: Last 5 Telemetry Readings
            TelemetryHistoryList(
                readings = uiState.recentReadings,
                cargoType = uiState.cargoType,
                contentColor = contentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Test Controls Quick Strip (allows testing Normal, Out of Bounds, Tamper)
            if (showTestingDrawer) {
                QuickSimulationControls(
                    onTriggerNormal = { viewModel.triggerSimulatedReading(4.2, false) },
                    onTriggerTempAlert = { viewModel.triggerSimulatedReading(9.5, false) },
                    onTriggerTamper = { viewModel.triggerSimulatedReading(4.2, true) },
                    contentColor = contentColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 4. CHAIN OF CUSTODY: Full-Width "Complete Trip" Action Button
            Button(
                onClick = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.openPinModal()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = animatedBackgroundColor
                )
            ) {
                Text(
                    text = "COMPLETE TRIP (CHAIN OF CUSTODY)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // =====================================================================
        // MODALS & DIALOGS
        // =====================================================================

        // Custody 4-Digit PIN Modal
        if (uiState.isPinModalVisible) {
            PinModalDialog(
                pinInput = uiState.pinInput,
                pinError = uiState.pinError,
                onDigitEntered = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.onPinDigitEntered(it)
                },
                onBackspace = {
                    VibrationHelper.vibrateClick(context)
                    viewModel.onPinBackspace()
                },
                onDismiss = { viewModel.dismissPinModal() }
            )
        }

        // Completed Trip Summary Modal
        if (uiState.isSummaryVisible) {
            TripSummaryDialog(
                tripId = uiState.latestReading?.tripId ?: "TRIP-8842-US",
                cargoType = uiState.cargoType,
                stats = uiState.sessionStats,
                onDismiss = { viewModel.dismissSummary() },
                onStartNewTrip = { viewModel.resetTripSession() }
            )
        }
    }
}

/**
 * Top bar displaying Trip ID, Battery icon with percentage, and Signal strength icon.
 */
@Composable
private fun DashboardTopBar(
    tripId: String,
    batteryPercentage: Int,
    signalStrength: Int,
    tint: Color,
    onToggleTestPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Trip ID with active status pulse
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint)
            )

            Text(
                text = tripId,
                color = tint,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Right side indicators: Signal, Battery, Dev toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SignalIcon(strengthPercentage = signalStrength, tint = tint)
            BatteryIcon(percentage = batteryPercentage, tint = tint)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    .clickable { onToggleTestPanel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Test Simulator Controls",
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Settings dropdown selector for switching between "Whole Blood" and "Platelets".
 */
@Composable
private fun CargoTypeDropdown(
    selectedType: CargoType,
    isExpanded: Boolean,
    tint: Color,
    onExpandChange: (Boolean) -> Unit,
    onTypeSelected: (CargoType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.5.dp,
                    color = tint.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { onExpandChange(true) }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = "CARGO TYPE / SAFE THRESHOLD",
                    color = tint.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${selectedType.displayName.uppercase()} (${selectedType.thresholdDisplay})",
                    color = tint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Cargo Type",
                tint = tint
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier
                .background(PureBlack)
                .border(1.dp, PureWhite, RoundedCornerShape(4.dp))
        ) {
            CargoType.entries.forEach { cargo ->
                val isSelected = cargo == selectedType
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = cargo.displayName,
                                    color = PureWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Safe Range: ${cargo.thresholdDisplay}",
                                    color = PureWhite.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = { onTypeSelected(cargo) }
                )
            }
        }
    }
}

/**
 * Center massive typography display for real-time temperature.
 */
@Composable
private fun RealtimeTemperatureDisplay(
    temperature: Double?,
    alertState: SystemAlertState,
    cargoType: CargoType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LIVE SENSOR READING",
            color = tint.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Massive Temperature Digits
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            val formattedValue = if (temperature != null) "%.1f".format(temperature) else "--.-"

            Text(
                text = formattedValue,
                color = tint,
                fontSize = 82.sp,
                lineHeight = 88.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-2.0).sp
            )

            Text(
                text = "°C",
                color = tint,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Badge
        val (statusText, statusBorderColor) = when (alertState) {
            SystemAlertState.NORMAL -> "STATUS: WITHIN SPEC (${cargoType.thresholdDisplay})" to tint.copy(alpha = 0.6f)
            SystemAlertState.TEMPERATURE_ALERT -> "EXCURSION: OUT OF SAFE BOUNDS" to tint
            SystemAlertState.TAMPER_ALERT -> "SECURITY: COMPROMISED ENCLOSURE" to tint
        }

        Box(
            modifier = Modifier
                .border(1.5.dp, statusBorderColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.0.sp
            )
        }
    }
}

/**
 * Quick simulation controls allowing manual triggering of telemetry scenarios for demo/QA.
 */
@Composable
private fun QuickSimulationControls(
    onTriggerNormal: () -> Unit,
    onTriggerTempAlert: () -> Unit,
    onTriggerTamper: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SIMULATOR QUICK TEST BAR",
            color = contentColor.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.0.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTriggerNormal,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor.copy(alpha = 0.15f),
                    contentColor = contentColor
                )
            ) {
                Text("Normal (4.2°)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onTriggerTempAlert,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor.copy(alpha = 0.15f),
                    contentColor = contentColor
                )
            ) {
                Text("Excursion (9.5°)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onTriggerTamper,
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor.copy(alpha = 0.15f),
                    contentColor = contentColor
                )
            ) {
                Text("Tamper Breach", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
