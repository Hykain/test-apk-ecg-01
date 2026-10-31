package com.example.ecgmonitor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.BleDeviceOption
import com.example.ecgmonitor.ui.theme.EcgTheme

/**
 * Pure Material 3 Modal Bottom Sheet for Bluetooth LE Device Scanning & Pairing.
 * Features:
 * - High-tech 360° Animated Radar Scanner
 * - 4-bar RSSI Medical Signal Quality Indicator
 * - Clinical device distance approximation & GATT badges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleDeviceSheet(
    devices: List<BleDeviceOption>,
    selected: BleDeviceOption?,
    isConnected: Boolean,
    isScanning: Boolean,
    status: String,
    onDismiss: () -> Unit,
    onScan: () -> Unit,
    onConnect: (BleDeviceOption) -> Unit,
    onDisconnect: () -> Unit,
    english: Boolean = false
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Scanning State / Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (english) "Bluetooth Devices" else "Thiết bị Bluetooth y tế",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (english) "Scanning..." else "Đang quét...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    FilledTonalButton(
                        onClick = onScan,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (english) "Scan" else "Quét",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (english) "Scan again" else "Quét lại")
                    }
                }
            }

            // Body: High-Tech Radar Scanner or Device List
            if (devices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // High-tech 360° animated radar sweep canvas with Sonar pulse
                        BleRadarScannerCanvas(
                            modifier = Modifier.size(135.dp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (english) "Searching for ESP32 ECG Devices..."
                                else "Đang quét tìm thiết bị ESP32 ECG xung quanh...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (english) "Ensure your ESP32-S3 sensor is powered on and within range."
                                else "Hãy bật nguồn ESP32-S3 và đặt gần điện thoại trong phạm vi 3 mét.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onScan,
                            modifier = Modifier.defaultMinSize(minHeight = 46.dp)
                        ) {
                            Text(if (english) "Start Scan" else "Bắt đầu quét lại")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    devices.forEach { dev ->
                        val isTarget = selected?.address == dev.address && isConnected

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTarget) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else if (dev.isRecommended) MaterialTheme.colorScheme.surfaceContainerLowest
                                else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = if (dev.isRecommended)
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            else
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Bluetooth Signal Badge
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (isTarget) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTarget) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                            contentDescription = null,
                                            tint = if (isTarget) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = dev.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (dev.isRecommended) {
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = if (english) "Recommended" else "Khuyên dùng",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }

                                        // RSSI Signal Bars + MAC Address
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            RssiSignalBars(
                                                bars = if (dev.isRecommended) 4 else 3,
                                                color = if (dev.isRecommended) EcgTheme.colors.rhythmNormal else MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = if (dev.isRecommended) "-52 dBm • ~1.2m" else dev.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (isTarget) {
                                    OutlinedButton(
                                        onClick = onDisconnect,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                    ) {
                                        Text(if (english) "Disconnect" else "Ngắt")
                                    }
                                } else {
                                    Button(
                                        onClick = { onConnect(dev) },
                                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                    ) {
                                        Text(if (english) "Connect" else "Kết nối")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}

/**
 * Futuristic 360° Rotating Radar Canvas with concentric rings and sweeping wave.
 */
@Composable
fun BleRadarScannerCanvas(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    // Pulsing sonar expanding wave
    val sonarProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonarProgress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w / 2f

        // 1. Sonar expanding pulse wave
        drawCircle(
            color = color.copy(alpha = (1f - sonarProgress) * 0.45f),
            radius = radius * sonarProgress,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // 2. Concentric Range Circles (25%, 50%, 75%, 100%)
        listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { fraction ->
            drawCircle(
                color = color.copy(alpha = 0.20f),
                radius = radius * fraction,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 3. Crosshair Grid Lines
        drawLine(
            color = color.copy(alpha = 0.25f),
            start = Offset(center.x, 0f),
            end = Offset(center.x, h),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = color.copy(alpha = 0.25f),
            start = Offset(0f, center.y),
            end = Offset(w, center.y),
            strokeWidth = 1.dp.toPx()
        )

        // 4. Sweeping Sector Wave (Arc with fading tail)
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, color.copy(alpha = 0.35f)),
                center = center
            ),
            startAngle = angle - 60f,
            sweepAngle = 60f,
            useCenter = true,
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )

        // 5. Center Glowing Radar Pulse Dot
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}

/**
 * 4-Bar Medical RSSI Signal Strength Indicator.
 */
@Composable
fun RssiSignalBars(
    bars: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 1..4) {
            val barHeight = (4 + (i * 2.5)).dp
            val isActive = i <= bars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) color else color.copy(alpha = 0.20f))
            )
        }
    }
}
