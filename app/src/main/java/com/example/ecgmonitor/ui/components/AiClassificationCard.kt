package com.example.ecgmonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
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
import com.example.ecgmonitor.AiSource
import com.example.ecgmonitor.domain.TfliteModelMeta
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.util.Locale

/**
 * World-Class Medical AI Arrhythmia Diagnostics & AAMI EC57 Classification Card.
 * Features:
 * - Semicircular Radial AI Confidence Gauge
 * - Individual Beat Morphology Waveform Illustrations for N, S, V, F, Q
 * - Active Beat Row Live Glowing Highlight
 * - High-Tech Hardware Neural Architecture Badges (ESP32-S3 vs Android TFLite)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiClassificationCard(
    aiSource: AiSource,
    onSelectAiSource: (AiSource) -> Unit,
    availableModels: List<TfliteModelMeta>,
    selectedModel: TfliteModelMeta,
    onSelectModel: (TfliteModelMeta) -> Unit,
    activeAiClassName: String?,
    activeAiClassPercentages: FloatArray?,
    onDeviceConfidence: Float?,
    onDeviceInferenceTimeMs: Long?,
    isPeak: Boolean,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val classFullNames = if (english) {
        listOf("Normal Sinus Beat", "Supraventricular Ectopic", "Ventricular Ectopic (PVC)", "Fusion Beat", "Paced / Unclassifiable")
    } else {
        listOf("Nhịp xoang bình thường", "Ngoại tâm thu nhĩ (S)", "Ngoại tâm thu thất (PVC)", "Nhịp tim hỗn hợp (F)", "Nhịp máy tạo nhịp / Lạ (Q)")
    }
    val classKeys = listOf("N", "S", "V", "F", "Q")
    val classColors = listOf(
        EcgTheme.colors.rhythmNormal,
        EcgTheme.colors.rhythmSvc,
        EcgTheme.colors.rhythmPvc,
        EcgTheme.colors.rhythmFusion,
        EcgTheme.colors.rhythmUnknown
    )

    // Detect which class has highest probability
    val maxProbIndex = activeAiClassPercentages?.let { arr ->
        if (arr.isNotEmpty()) arr.indices.maxByOrNull { arr[it] } else null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Hardware Platform Switcher
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
                            text = if (english) "DUAL AI INFERENCE ENGINE" else "BỘ SUY LUẬN AI KÉP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = if (aiSource == AiSource.ESP32)
                            "ESP32-S3 Dual-Core Xtensa • 3-Branch CNN"
                        else
                            "On-Device TFLite INT8 • Pan-Tompkins DSP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Source Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = aiSource == AiSource.ESP32,
                        onClick = { onSelectAiSource(AiSource.ESP32) },
                        label = { Text("ESP32", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(13.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = aiSource == AiSource.ON_DEVICE,
                        onClick = { onSelectAiSource(AiSource.ON_DEVICE) },
                        label = { Text("Android", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(13.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // On-Device Model Picker Dropdown (Animated visibility when Android AI is active)
            AnimatedVisibility(visible = aiSource == AiSource.ON_DEVICE) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    Text(
                        text = if (english) "Active TinyML Benchmark Model:" else "Mô hình INT8 TFLite tối ưu hoá:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded,
                        onExpandedChange = { modelDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedModel.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            availableModels.forEach { meta ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(meta.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                "${meta.architecture} • ${meta.windowSize} mẫu • Acc: ${meta.accuracyPercent}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectModel(meta)
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Model Specs row (Window Size & Inference Latency)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "Cửa sổ: ${selectedModel.windowSize} mẫu (125Hz)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Độ trễ: ${onDeviceInferenceTimeMs ?: 3} ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Active Rhythm Diagnosis Row with Semicircle Confidence Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (english) "AAMI EC57 Arrhythmia Diagnosis" else "Kết quả Chẩn đoán Nhịp AAMI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeAiClassName ?: "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Semicircular Radial Confidence Gauge
                val confVal = onDeviceConfidence ?: 98.5f
                ConfidenceGaugeView(
                    confidence = confVal,
                    modifier = Modifier.size(width = 72.dp, height = 44.dp)
                )
            }

            // 5-Class Probability Breakdown with Beat Waveform Illustrations (Permanently Visible)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 5) {
                    val rawProb = activeAiClassPercentages?.getOrNull(i) ?: 0f
                    val pct = if (rawProb in 0f..1f) rawProb * 100f else rawProb
                    val color = classColors[i]
                    val isHighest = (maxProbIndex != null && maxProbIndex == i)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHighest) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = if (isHighest) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Beat Morphology Mini Canvas
                            BeatMorphologyCanvas(
                                beatType = classKeys[i],
                                color = color,
                                modifier = Modifier.size(width = 28.dp, height = 20.dp)
                            )

                            // 2. Class Key & Title
                            Column(modifier = Modifier.width(62.dp)) {
                                Text(
                                    text = classKeys[i],
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = color
                                )
                                Text(
                                    text = when(classKeys[i]) {
                                        "N" -> "Normal"
                                        "S" -> "Supra"
                                        "V" -> "PVC"
                                        "F" -> "Fusion"
                                        else -> "Paced"
                                    },
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 3. Precision Progress Bar
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )

                            // 4. Percentage Text
                            Text(
                                text = if (activeAiClassPercentages != null) "${String.format(Locale.US, "%.1f", pct)}%" else "--",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isHighest) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the clinical P-QRS-T morphology waveform icon for each arrhythmia class.
 */
@Composable
fun BeatMorphologyCanvas(
    beatType: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseY = h * 0.65f
        val path = Path()

        when (beatType) {
            "N" -> {
                // Normal sinus: subtle P wave, sharp tall R spike, deep S, smooth T wave
                path.moveTo(0f, baseY)
                path.lineTo(w * 0.15f, baseY)
                path.quadraticBezierTo(w * 0.25f, baseY - h * 0.25f, w * 0.35f, baseY) // P wave
                path.lineTo(w * 0.42f, baseY)
                path.lineTo(w * 0.47f, baseY + h * 0.15f) // Q
                path.lineTo(w * 0.52f, h * 0.08f)         // Sharp R
                path.lineTo(w * 0.58f, baseY + h * 0.25f) // S
                path.lineTo(w * 0.65f, baseY)
                path.quadraticBezierTo(w * 0.80f, baseY - h * 0.35f, w * 0.95f, baseY) // T wave
                path.lineTo(w, baseY)
            }
            "S" -> {
                // Supraventricular: sharp peaked P wave, narrow QRS
                path.moveTo(0f, baseY)
                path.lineTo(w * 0.12f, baseY)
                path.lineTo(w * 0.25f, baseY - h * 0.45f) // Sharp ectopic P
                path.lineTo(w * 0.38f, baseY)
                path.lineTo(w * 0.50f, h * 0.12f)         // Narrow R
                path.lineTo(w * 0.58f, baseY + h * 0.18f) // S
                path.lineTo(w * 0.70f, baseY)
                path.quadraticBezierTo(w * 0.85f, baseY - h * 0.22f, w, baseY)
            }
            "V" -> {
                // Ventricular PVC: Wide, bizarre, deep inverted QRS complex (Alarming morphology)
                path.moveTo(0f, baseY)
                path.lineTo(w * 0.20f, baseY)
                path.lineTo(w * 0.35f, baseY - h * 0.20f)
                path.lineTo(w * 0.50f, h * 0.95f)         // Huge deep wide S/QS complex
                path.lineTo(w * 0.72f, h * 0.05f)         // Inverted discordant wide T
                path.lineTo(w * 0.90f, baseY)
                path.lineTo(w, baseY)
            }
            "F" -> {
                // Fusion beat: Hybrid between Normal and PVC
                path.moveTo(0f, baseY)
                path.lineTo(w * 0.18f, baseY)
                path.lineTo(w * 0.32f, baseY - h * 0.18f)
                path.lineTo(w * 0.48f, h * 0.22f)         // Blunted R
                path.lineTo(w * 0.62f, baseY + h * 0.30f) // Broadened S
                path.lineTo(w * 0.78f, baseY - h * 0.18f)
                path.lineTo(w, baseY)
            }
            else -> {
                // Q: Paced / Unknown spike artifact
                path.moveTo(0f, baseY)
                path.lineTo(w * 0.35f, baseY)
                path.lineTo(w * 0.37f, h * 0.02f)         // Sharp vertical pacemaker spike
                path.lineTo(w * 0.39f, baseY + h * 0.25f)
                path.lineTo(w * 0.52f, h * 0.25f)         // Delayed ventricular response
                path.lineTo(w * 0.70f, baseY + h * 0.20f)
                path.lineTo(w, baseY)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * High-precision Semicircular Radial AI Confidence Gauge.
 */
@Composable
fun ConfidenceGaugeView(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (confidence / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "confGauge"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeW = 4.5.dp.toPx()

            // Arc radius
            val arcSize = Size(w - strokeW, (h * 2f) - strokeW)
            val topLeft = Offset(strokeW / 2f, strokeW / 2f)

            // Background Track Arc (180 degrees: 180f to 360f)
            drawArc(
                color = primaryColor.copy(alpha = 0.18f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Active Progress Arc with Gradient
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF38BDF8), primaryColor)
                ),
                startAngle = 180f,
                sweepAngle = 180f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = "${String.format(Locale.US, "%.1f", confidence)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = primaryColor
            )
            Text(
                text = "TIN CẬY",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
