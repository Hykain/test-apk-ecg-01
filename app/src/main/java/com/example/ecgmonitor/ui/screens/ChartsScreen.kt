package com.example.ecgmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.BleDeviceOption
import com.example.ecgmonitor.domain.EcgAssessment
import com.example.ecgmonitor.ui.components.EcgChartCard
import com.example.ecgmonitor.ui.theme.EcgTheme

/**
 * Material 3 Multi-Waveform ECG Analysis Screen.
 * Displays 3 synchronized telemetry channels:
 * 1. Raw ADC signal from AD8232 sensor.
 * 2. ESP32 hardware filtered signal (IIR Notch 50Hz + baseline wander removal).
 * 3. Android local DSP filtered signal (RealtimeEcgFilter comparison).
 */
@Composable
fun ChartsScreen(
    raw: List<Float>,
    espFiltered: List<Float>,
    androidFiltered: List<Float>,
    status: String,
    assessment: EcgAssessment,
    spo2: Float?,
    ppgBpm: Float?,
    aiClassName: String?,
    selected: BleDeviceOption?,
    isConnected: Boolean,
    samples: Int,
    onOpenConnect: () -> Unit,
    disconnect: () -> Unit,
    save: () -> Unit,
    onExportCurrentCsv: () -> Unit,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Connection Status Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (isConnected) EcgTheme.colors.rhythmNormal else MaterialTheme.colorScheme.error,
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selected?.name ?: if (english) "Device Active" else "Thiết bị",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onOpenConnect,
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isConnected) (if (english) "Switch" else "Đổi")
                            else (if (english) "Connect" else "Kết nối")
                        )
                    }
                    if (isConnected) {
                        OutlinedButton(
                            onClick = disconnect,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 44.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (english) "Disconnect" else "Ngắt")
                        }
                    }
                }
            }
        }

        // Section Title: Multi-Waveform Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (english) "Multi-Channel ECG Analysis" else "Phân tích Đa kênh Đồ thị ECG",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "3 CHANNELS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        // Waveform 1: Raw Signal from AD8232 ADC
        EcgChartCard(
            channelNumber = 1,
            title = if (english) "Raw Signal (AD8232 ADC)" else "Tín hiệu Thô (AD8232 ADC)",
            subtitle = if (english) "Direct ADC samples with 50Hz mains & baseline wander"
            else "Tín hiệu thô trực tiếp từ cảm biến AD8232 (bao gồm nhiễu 50Hz)",
            samples = raw,
            waveformColor = EcgTheme.colors.rawChannel
        )

        // Waveform 2: Filtered on ESP32-S3 Hardware Core
        EcgChartCard(
            channelNumber = 2,
            title = if (english) "ESP32 DSP Filtered (Notch 50Hz)" else "Lọc trên chip ESP32 (Notch 50Hz)",
            subtitle = if (english) "Hardware DSP: IIR Notch 50Hz + baseline wander removal"
            else "Xử lý số tín hiệu trên vi điều khiển ESP32: Lọc Notch 50Hz + chống trôi nền",
            samples = espFiltered,
            waveformColor = EcgTheme.colors.espFilteredChannel,
            isPeak = assessment.bpm != null
        )

        // Waveform 3: Filtered on Android Device
        EcgChartCard(
            channelNumber = 3,
            title = if (english) "Android Local Filtered (DSP)" else "Lọc trên máy Android (Kiểm chứng)",
            subtitle = if (english) "Computed in Kotlin for realtime filter validation"
            else "Xử lý bằng bộ lọc RealtimeEcgFilter cục bộ để đối chiếu chất lượng",
            samples = androidFiltered,
            waveformColor = EcgTheme.colors.androidFilteredChannel
        )

        // Telemetry Snapshot Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (english) "Session Telemetry Summary" else "Thông số Kỹ thuật Phiên đo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (english) "Heart Rate" else "Nhịp tim",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = assessment.bpm?.let { "$it BPM" } ?: "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcgTheme.colors.peakMarker
                        )
                    }

                    Column {
                        Text(
                            text = "SpO₂",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = spo2?.let { "$it%" } ?: "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcgTheme.colors.spo2Color
                        )
                    }

                    Column {
                        Text(
                            text = if (english) "PPG Pulse" else "Mạch quang",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ppgBpm?.let { "${it.toInt()} BPM" } ?: "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcgTheme.colors.ppgBpmColor
                        )
                    }

                    Column {
                        Text(
                            text = if (english) "Duration" else "Thời lượng",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${samples / 125}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Text(
                    text = "AI Class: ${aiClassName ?: "--"}  •  125 Hz Sampling  •  ${samples} total samples",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons: Save to DB & Export CSV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExportCurrentCsv,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                enabled = espFiltered.isNotEmpty()
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (english) "Export .CSV" else "Xuất .CSV")
            }

            Button(
                onClick = save,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                enabled = espFiltered.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (english) "Save to DB" else "Lưu CSDL")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

