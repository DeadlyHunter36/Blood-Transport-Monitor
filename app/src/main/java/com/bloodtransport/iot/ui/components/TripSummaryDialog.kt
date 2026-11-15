package com.bloodtransport.iot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bloodtransport.iot.data.model.CargoType
import com.bloodtransport.iot.ui.screen.SessionStats
import com.bloodtransport.iot.ui.theme.PureBlack
import com.bloodtransport.iot.ui.theme.PureWhite

/**
 * Summary Screen displayed upon successful 4-digit PIN authentication.
 * Presents Max Temp, Min Temp, and Tamper Count for the completed transport session.
 */
@Composable
fun TripSummaryDialog(
    tripId: String,
    cargoType: CargoType,
    stats: SessionStats,
    onDismiss: () -> Unit,
    onStartNewTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, PureWhite, RoundedCornerShape(8.dp)),
            color = PureBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Text(
                    text = "CHAIN OF CUSTODY VERIFIED",
                    color = PureBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .background(PureWhite, RoundedCornerShape(2.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "TRIP AUDIT SUMMARY",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Session: $tripId | ${cargoType.displayName}",
                    color = PureWhite.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Max Temp
                    MetricBox(
                        title = "MAX TEMP",
                        value = stats.displayMaxTemp,
                        modifier = Modifier.weight(1f)
                    )

                    // Min Temp
                    MetricBox(
                        title = "MIN TEMP",
                        value = stats.displayMinTemp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tamper Count
                    MetricBox(
                        title = "TAMPER COUNT",
                        value = "${stats.tamperCount} EVENTS",
                        isAlert = stats.tamperCount > 0,
                        modifier = Modifier.weight(1f)
                    )

                    // Total Logs
                    MetricBox(
                        title = "TOTAL PACKETS",
                        value = "${stats.totalReadingsCount} LOGS",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Button(
                    onClick = onStartNewTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "START NEW TRIP",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.0.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PureWhite
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PureWhite.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "CLOSE & RESUME VIEW",
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (isAlert) Color(0xFFD32F2F) else PureWhite.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isAlert) Color(0xFF330000) else Color(0xFF101010))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (isAlert) Color(0xFFFF5252) else PureWhite.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            color = PureWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}
