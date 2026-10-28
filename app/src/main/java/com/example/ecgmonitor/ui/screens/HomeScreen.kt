package com.example.ecgmonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.AiSource
import com.example.ecgmonitor.domain.EcgAssessment
import com.example.ecgmonitor.domain.TfliteModelMeta
import com.example.ecgmonitor.ui.components.AiClassificationCard
import com.example.ecgmonitor.ui.components.AmiCompanionCard
import com.example.ecgmonitor.ui.components.VitalSignsOverview

/**
 * Material 3 Home Dashboard Screen.
 * Presents live telemetry, Ami mascot insights, and edge AI arrhythmia diagnostics.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    assessment: EcgAssessment,
    spo2: Float?,
    ppgBpm: Float?,
    activeAiClassName: String?,
    activeAiClassPercentages: FloatArray?,
    aiSource: AiSource,
    onSelectAiSource: (AiSource) -> Unit,
    availableModels: List<TfliteModelMeta>,
    selectedModel: TfliteModelMeta,
    onSelectModel: (TfliteModelMeta) -> Unit,
    onDeviceConfidence: Float?,
    onDeviceInferenceTimeMs: Long?,
    samples: Int,
    gifAsset: String,
    isPeak: Boolean,
    isConnected: Boolean,
    isDemoMode: Boolean,
    onToggleDemo: () -> Unit,
    onOpenConnect: () -> Unit,
    english: Boolean = false
) {
    val advice = if (!isConnected || assessment.bpm == null) {
        if (english) "Connect your ESP32-S3 ECG sensor or turn on Demo Mode to inspect real-time heart signals."
        else "Hãy kết nối thiết bị ESP32 ECG hoặc bật Chế độ chạy thử (Demo) để bắt đầu theo dõi sức khoẻ nhé."
    } else {
        assessment.advice
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Connection Banner when Disconnected
        AnimatedVisibility(visible = !isConnected) {
            Card(
                onClick = onOpenConnect,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
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
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (english) "Connect ECG Device" else "Kết nối thiết bị ECG",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (english) "Pair ESP32-S3 via BLE or run offline simulation"
                                else "Ghép đôi ESP32-S3 qua BLE hoặc chạy mô phỏng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onToggleDemo,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (english) "Demo" else "Thử")
                        }
                        Button(
                            onClick = onOpenConnect,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        ) {
                            Text(if (english) "Pair" else "Kết nối")
                        }
                    }
                }
            }
        }

        // 2. Mascot Ami Advice Card
        AmiCompanionCard(
            advice = advice,
            gifAsset = gifAsset,
            english = english
        )

        // 3. Vital Signs Overview (Heart Rate BPM, SpO2, PPG Pulse)
        VitalSignsOverview(
            bpm = assessment.bpm?.toFloat(),
            spo2 = spo2,
            ppgBpm = ppgBpm,
            isPeak = isPeak,
            english = english
        )

        // 4. Dual AI Arrhythmia Classification Card
        AiClassificationCard(
            aiSource = aiSource,
            onSelectAiSource = onSelectAiSource,
            availableModels = availableModels,
            selectedModel = selectedModel,
            onSelectModel = onSelectModel,
            activeAiClassName = activeAiClassName ?: (if (assessment.bpm == null) "--" else assessment.title),
            activeAiClassPercentages = activeAiClassPercentages,
            onDeviceConfidence = onDeviceConfidence,
            onDeviceInferenceTimeMs = onDeviceInferenceTimeMs,
            isPeak = isPeak,
            english = english
        )

        Spacer(Modifier.height(8.dp))
    }
}

