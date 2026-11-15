package com.bloodtransport.iot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.bloodtransport.iot.ui.theme.PureBlack
import com.bloodtransport.iot.ui.theme.PureWhite

/**
 * High-contrast modal dialog prompting for 4-digit Custody PIN verification.
 */
@Composable
fun PinModalDialog(
    pinInput: String,
    pinError: String?,
    onDigitEntered: (Char) -> Unit,
    onBackspace: () -> Unit,
    onDismiss: () -> Unit,
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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHAIN OF CUSTODY",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Authorized custodian PIN required to complete trip log.",
                    color = PureWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4-Digit PIN Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(2.dp, PureWhite, CircleShape)
                                .background(if (isFilled) PureWhite else Color.Transparent)
                        )
                    }
                }

                // Error Message Display
                if (pinError != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = pinError,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PureWhite, RoundedCornerShape(4.dp))
                            .background(Color(0xFF8B0000))
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "HINT: DEMO PIN = 1234",
                        color = PureWhite.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Clean Flat Numeric Keypad
                val keypadLayout = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9'),
                    listOf('X', '0', '<')
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    keypadLayout.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { char ->
                                when (char) {
                                    'X' -> {
                                        // Blank spacer key
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                    '<' -> {
                                        // Backspace key
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .border(1.dp, PureWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .clickable { onBackspace() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = PureWhite
                                            )
                                        }
                                    }
                                    else -> {
                                        // Numeric Key
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .border(1.dp, PureWhite, RoundedCornerShape(4.dp))
                                                .clickable { onDigitEntered(char) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char.toString(),
                                                color = PureWhite,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
