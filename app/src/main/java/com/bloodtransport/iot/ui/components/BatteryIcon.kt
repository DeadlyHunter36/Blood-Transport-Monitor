package com.bloodtransport.iot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal flat vector battery indicator with dynamic fill and percentage label.
 */
@Composable
fun BatteryIcon(
    percentage: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val clamped = percentage.coerceIn(0, 100)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Canvas(
            modifier = Modifier
                .width(22.dp)
                .height(11.dp)
        ) {
            val strokeWidth = 1.5.dp.toPx()
            val capWidth = 2.dp.toPx()
            val capHeight = 4.dp.toPx()
            val bodyWidth = size.width - capWidth - 2.dp.toPx()
            val bodyHeight = size.height

            // Outer battery shell
            drawRoundRect(
                color = tint,
                topLeft = Offset(0f, 0f),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Positive terminal nub
            drawRoundRect(
                color = tint,
                topLeft = Offset(bodyWidth + 1.dp.toPx(), (bodyHeight - capHeight) / 2f),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )

            // Inner fill bar
            val innerPadding = 2.5.dp.toPx()
            val maxFillWidth = bodyWidth - (innerPadding * 2)
            val fillWidth = (maxFillWidth * (clamped / 100f)).coerceAtLeast(0f)
            val fillHeight = bodyHeight - (innerPadding * 2)

            if (fillWidth > 0f) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(innerPadding, innerPadding),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        Text(
            text = "$clamped%",
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
