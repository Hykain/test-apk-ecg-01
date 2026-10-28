package com.example.ecgmonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.AiSource
import com.example.ecgmonitor.domain.TfliteModelMeta
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.util.Locale

/**
 * Pure Material 3 Dual AI Diagnostics & AAMI EC57 Arrhythmia Classification Card.
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

    val classNames = if (english) {
        listOf("Normal (N)", "Supraventricular (S)", "Ventricular / PVC (V)", "Fusion (F)", "Unknown (Q)")
    } else {
        listOf("Bình thường (N)", "Ngoại tâm thất nhĩ (S)", "Ngoại tâm thất (V)", "Nhịp hỗn hợp (F)", "Không xác định (Q)")
    }
    val classKeys = listOf("N", "S", "V", "F", "Q")
    val classColors = listOf(
        EcgTheme.colors.rhythmNormal,
        EcgTheme.colors.rhythmSvc,
        EcgTheme.colors.rhythmPvc,
        EcgTheme.colors.rhythmFusion,
        EcgTheme.colors.rhythmUnknown
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Engine Switcher (ESP32 vs Android On-Device)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (english) "DUAL AI INFERENCE ENGINE" else "BỘ SUY LUẬN AI KÉP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (english) "Select inference hardware platform"
                        else "Chuyển đổi nền tảng phần cứng phân loại",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Source Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = aiSource == AiSource.ESP32,
                        onClick = { onSelectAiSource(AiSource.ESP32) },
                        label = { Text("ESP32", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = aiSource == AiSource.ON_DEVICE,
                        onClick = { onSelectAiSource(AiSource.ON_DEVICE) },
                        label = { Text("Android", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // On-Device Model Picker Dropdown (Animated visibility when Android AI is active)
            AnimatedVisibility(visible = aiSource == AiSource.ON_DEVICE) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Text(
                        text = if (english) "Quantized INT8 TFLite Model:" else "Mô hình INT8 TFLite trên điện thoại:",
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
                                text = "Độ trễ: ${onDeviceInferenceTimeMs ?: 4} ms",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Active Rhythm Diagnosis Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (english) "AAMI Arrhythmia Diagnostic" else "Kết quả Chẩn đoán Nhịp AAMI",
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

                if (onDeviceConfidence != null && aiSource == AiSource.ON_DEVICE) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", onDeviceConfidence)}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 5-Class Probability Breakdown
            if (activeAiClassPercentages != null && activeAiClassPercentages.size >= 5) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until 5) {
                        val rawProb = activeAiClassPercentages.getOrNull(i) ?: 0f
                        val pct = if (rawProb in 0f..1f) rawProb * 100f else rawProb
                        val color = classColors[i]

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = classKeys[i],
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = color,
                                modifier = Modifier.width(22.dp)
                            )

                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "${String.format(Locale.US, "%.1f", pct)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(46.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

