package com.example.ecgmonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.util.Locale

/**
 * High-End Material 3 Clinical Vital Signs Dashboard.
 * Features:
 * - Heartbeat ripple aura & scale animation on R-Peak detection
 * - Radial Arc Gauge for blood oxygen (SpO2 %)
 * - Tabular monospaced figures to eliminate high-frequency number jitter
 * - Clinical status evaluation pills (Optimal, Warning, Critical)
 */
@Composable
fun VitalSignsOverview(
    bpm: Float?,
    spo2: Float?,
    ppgBpm: Float?,
    isPeak: Boolean,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Telemetry Hardware Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(EcgTheme.colors.espFilteredChannel, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (english) "VITAL SIGNS TELEMETRY" else "CHỈ SỐ SINH HIỆU THỜI GIAN THỰC",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "AD8232 • MAX30102",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // 3 High-Tech Metric Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. ECG Heart Rate with Ripple Aura
                val hrStatus = when {
                    bpm == null -> if (english) "Standby" else "Chờ đo"
                    bpm < 50f -> if (english) "Bradycardia" else "Nhịp chậm"
                    bpm > 100f -> if (english) "Tachycardia" else "Nhịp nhanh"
                    else -> if (english) "Normal" else "Bình thường"
                }

                HeartRateMetricItem(
                    label = if (english) "Heart Rate" else "Nhịp tim ECG",
                    bpm = bpm,
                    statusText = hrStatus,
                    isPeak = isPeak,
                    modifier = Modifier.weight(1f)
                )

                // 2. Blood Oxygen SpO2 with Radial Arc Gauge
                val spo2Color = when {
                    spo2 == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    spo2 >= 95f -> EcgTheme.colors.rhythmNormal
                    spo2 >= 90f -> EcgTheme.colors.rhythmSvc
                    else -> EcgTheme.colors.rhythmPvc
                }

                val spo2Status = when {
                    spo2 == null -> if (english) "Standby" else "Chờ đo"
                    spo2 >= 95f -> if (english) "Optimal" else "Tối ưu"
                    spo2 >= 90f -> if (english) "Attention" else "Cần chú ý"
                    else -> if (english) "Hypoxia" else "Thiếu oxy"
                }

                Spo2MetricItem(
                    label = "SpO₂",
                    spo2 = spo2,
                    statusText = spo2Status,
                    accentColor = spo2Color,
                    modifier = Modifier.weight(1f)
                )

                // 3. PPG Optical Pulse BPM
                val ppgStatus = when {
                    ppgBpm == null -> if (english) "Standby" else "Chờ đo"
                    else -> if (english) "Optical Sync" else "Đồng bộ quang"
                }

                PpgPulseMetricItem(
                    label = if (english) "PPG Pulse" else "Mạch quang",
                    ppgBpm = ppgBpm,
                    statusText = ppgStatus,
                    accentColor = EcgTheme.colors.ppgBpmColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * ECG Heart Rate Tile with Heartbeat Pulse Ripple & Glow Aura.
 */
@Composable
fun HeartRateMetricItem(
    label: String,
    bpm: Float?,
    statusText: String,
    isPeak: Boolean,
    modifier: Modifier = Modifier
) {
    val heartScale by animateFloatAsState(
        targetValue = if (isPeak) 1.35f else 1.0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "heartScale"
    )

    val auraAlpha by animateFloatAsState(
        targetValue = if (isPeak) 0.28f else 0.0f,
        animationSpec = tween(durationMillis = 200),
        label = "auraAlpha"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = if (isPeak) androidx.compose.foundation.BorderStroke(1.2.dp, EcgTheme.colors.peakMarker.copy(alpha = 0.6f)) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcgTheme.colors.peakMarker.copy(alpha = auraAlpha))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = EcgTheme.colors.peakMarker,
                        modifier = Modifier
                            .size(15.dp)
                            .scale(heartScale)
                    )
                }

                // Value Display (Monospaced to stop number jitter)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = bpm?.toInt()?.toString() ?: "--",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = EcgTheme.colors.peakMarker,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                // Clinical Status Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EcgTheme.colors.peakMarker.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcgTheme.colors.peakMarker,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * SpO2 Metric Item with Interactive Circular Progress Ring.
 */
@Composable
fun Spo2MetricItem(
    label: String,
    spo2: Float?,
    statusText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = ((spo2 ?: 0f) / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "spo2Gauge"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.Opacity,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Value Display with Mini Arc Progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mini Radial Gauge
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3.dp.toPx()
                        // Background track arc (240 degrees)
                        drawArc(
                            color = accentColor.copy(alpha = 0.18f),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Active progress arc
                        drawArc(
                            color = accentColor,
                            startAngle = 150f,
                            sweepAngle = 240f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = spo2?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                    )
                }
            }

            // Clinical Status Pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = statusText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Optical PPG Pulse Metric Item with Waveform Icon & Status.
 */
@Composable
fun PpgPulseMetricItem(
    label: String,
    ppgBpm: Float?,
    statusText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Value Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = ppgBpm?.toInt()?.toString() ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            // Clinical Status Pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = statusText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}
