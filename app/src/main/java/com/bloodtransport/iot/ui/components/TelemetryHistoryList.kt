package com.bloodtransport.iot.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodtransport.iot.data.model.CargoType
import com.bloodtransport.iot.data.model.TelemetryLog

/**
 * Clean minimal table displaying the rolling buffer of the last 5 telemetry logs.
 */
@Composable
fun TelemetryHistoryList(
    readings: List<TelemetryLog>,
    cargoType: CargoType,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT TELEMETRY (ROLLING 5)",
                color = contentColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
            )
            Text(
                text = "${readings.size}/5 LOGS",
                color = contentColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (readings.isEmpty()) {
            Text(
                text = "Awaiting initial sensor packets...",
                color = contentColor.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            readings.forEachIndexed { index, log ->
                val isSafe = cargoType.isSafe(log.temperature)
                val statusText = when {
                    log.isTampered -> "TAMPERED"
                    !isSafe -> "EXCURSION"
                    else -> "OPTIMAL"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = contentColor.copy(alpha = if (index == 0) 0.6f else 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (index == 0) "NOW" else "-${index}m",
                            color = contentColor.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(36.dp)
                        )

                        Text(
                            text = log.formattedTime,
                            color = contentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = log.formattedTemperature,
                            color = contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = statusText,
                            color = if (statusText != "OPTIMAL") contentColor else contentColor.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = contentColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
