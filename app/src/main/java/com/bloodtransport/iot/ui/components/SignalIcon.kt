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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal 4-bar cellular signal strength vector indicator.
 */
@Composable
fun SignalIcon(
    strengthPercentage: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val activeBars = when {
        strengthPercentage >= 75 -> 4
        strengthPercentage >= 50 -> 3
        strengthPercentage >= 25 -> 2
        strengthPercentage > 0 -> 1
        else -> 0
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .height(11.dp)
        ) {
            val totalBars = 4
            val spacing = 2.dp.toPx()
            val barWidth = (size.width - (spacing * (totalBars - 1))) / totalBars
            val heights = floatArrayOf(0.3f, 0.5f, 0.75f, 1.0f)

            for (i in 0 until totalBars) {
                val barHeight = size.height * heights[i]
                val x = i * (barWidth + spacing)
                val y = size.height - barHeight
                val isFilled = i < activeBars
                val barColor = if (isFilled) tint else tint.copy(alpha = 0.25f)

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(0.8.dp.toPx(), 0.8.dp.toPx())
                )
            }
        }

        Text(
            text = "$strengthPercentage%",
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
