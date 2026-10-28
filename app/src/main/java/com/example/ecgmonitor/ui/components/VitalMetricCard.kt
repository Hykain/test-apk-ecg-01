package com.example.ecgmonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.util.Locale

/**
 * Pure Material 3 Vital Signs Telemetry Cards.
 * Implements "KPIs are type, not tiles" and "Color on data, not chrome".
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
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Sensor Telemetry source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (english) "VITAL SIGNS TELEMETRY" else "CHỈ SỐ SINH HIỆU THỜI GIAN THỰC",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "AD8232 + MAX30102",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // 3-Metric Horizontal Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. ECG Heart Rate BPM
                VitalMetricItem(
                    label = if (english) "ECG Heart Rate" else "Nhịp tim ECG",
                    value = bpm?.toInt()?.toString() ?: "--",
                    unit = "BPM",
                    icon = Icons.Default.Favorite,
                    accentColor = EcgTheme.colors.peakMarker,
                    isPulsing = isPeak,
                    modifier = Modifier.weight(1f)
                )

                // 2. Blood Oxygen SpO2 %
                val spo2Color = when {
                    spo2 == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    spo2 >= 95f -> EcgTheme.colors.rhythmNormal
                    spo2 >= 90f -> EcgTheme.colors.rhythmSvc
                    else -> EcgTheme.colors.rhythmPvc
                }

                VitalMetricItem(
                    label = "SpO₂",
                    value = spo2?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                    unit = "%",
                    icon = Icons.Default.Opacity,
                    accentColor = spo2Color,
                    isPulsing = false,
                    modifier = Modifier.weight(1f)
                )

                // 3. PPG Optical Pulse BPM
                VitalMetricItem(
                    label = if (english) "PPG Pulse" else "Mạch quang",
                    value = ppgBpm?.toInt()?.toString() ?: "--",
                    unit = "BPM",
                    icon = Icons.Default.Waves,
                    accentColor = EcgTheme.colors.ppgBpmColor,
                    isPulsing = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun VitalMetricItem(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    isPulsing: Boolean,
    modifier: Modifier = Modifier
) {
    val heartScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.25f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "heartScale"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .size(14.dp)
                        .scale(heartScale)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 26.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

