package com.example.ecgmonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.BleDeviceOption
import com.example.ecgmonitor.domain.EcgAssessment
import com.example.ecgmonitor.ui.components.EcgChartCard
import com.example.ecgmonitor.ui.components.Stacked3LeadChartCard
import com.example.ecgmonitor.ui.theme.EcgTheme

/**
 * Material 3 Multi-Waveform ECG Analysis Screen.
 * Features:
 * - Interactive Channel Selector Filter Chips (All 3 Channels, ESP32 Filtered, Raw AD8232, Android DSP)
 * - Clinical Calibration Bar (Paper Speed 25/50 mm/s, Voltage Gain 5/10/20 mm/mV)
 * - Telemetry Snapshot Summary Card with Monospaced Tabular Figures
 * - Storage Access Framework (SAF) CSV Export and Room DB Save
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
    var isStackedView by rememberSaveable { mutableStateOf(true) }
    var selectedChannelFilter by rememberSaveable { mutableIntStateOf(0) } // 0: All, 1: Raw, 2: ESP32, 3: Android
    var paperSpeed by rememberSaveable { mutableStateOf("25 mm/s") }
    var voltageGain by rememberSaveable { mutableStateOf("10 mm/mV") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Connection Status Banner Card
        Card(
            shape = RoundedCornerShape(18.dp),
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

        // 2. Clinical Calibration Control Bar (Speed & Gain Settings)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (english) "CALIBRATION" else "CHUẨN ĐỘ Y KHOA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Speed Toggle
                    Surface(
                        onClick = { paperSpeed = if (paperSpeed == "25 mm/s") "50 mm/s" else "25 mm/s" },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = paperSpeed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Gain Toggle
                    Surface(
                        onClick = {
                            voltageGain = when (voltageGain) {
                                "10 mm/mV" -> "20 mm/mV"
                                "20 mm/mV" -> "5 mm/mV"
                                else -> "10 mm/mV"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = voltageGain,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Filter Notch Status Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "NOTCH 50Hz: ON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. View Mode Toggle (Stacked 3-Lead vs Separate Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isStackedView,
                onClick = { isStackedView = true },
                label = {
                    Text(
                        text = if (english) "Stacked 3-Lead Sync" else "Xếp tầng 3 Đạo trình",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(15.dp))
                },
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = !isStackedView,
                onClick = { isStackedView = false },
                label = {
                    Text(
                        text = if (english) "Separate Cards" else "Từng Thẻ Riêng",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(15.dp))
                },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 4. Waveforms Display: Either Stacked 3-Lead Canvas or Filtered Individual Cards
        if (isStackedView) {
            Stacked3LeadChartCard(
                raw = raw,
                espFiltered = espFiltered,
                androidFiltered = androidFiltered,
                paperSpeed = paperSpeed,
                voltageGain = voltageGain,
                isPeak = assessment.bpm != null,
                english = english
            )
        } else {
            // Channel Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterLabels = if (english) {
                    listOf("All Channels", "CH 2: ESP32", "CH 1: Raw", "CH 3: Android")
                } else {
                    listOf("Tất cả 3 kênh", "CH 2: Lọc ESP32", "CH 1: Thô AD8232", "CH 3: Lọc Android")
                }

                filterLabels.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedChannelFilter == idx,
                        onClick = { selectedChannelFilter = idx },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Channel 1: Raw ADC
            if (selectedChannelFilter == 0 || selectedChannelFilter == 2) {
                EcgChartCard(
                    channelNumber = 1,
                    title = if (english) "Raw Signal (AD8232 ADC)" else "Tín hiệu Thô (AD8232 ADC)",
                    subtitle = if (english) "Direct ADC samples with 50Hz mains & baseline wander"
                    else "Tín hiệu thô trực tiếp từ cảm biến AD8232 (bao gồm nhiễu 50Hz)",
                    samples = raw,
                    waveformColor = EcgTheme.colors.rawChannel,
                    height = if (selectedChannelFilter != 0) 240.dp else 195.dp
                )
            }

            // Channel 2: Filtered on ESP32
            if (selectedChannelFilter == 0 || selectedChannelFilter == 1) {
                EcgChartCard(
                    channelNumber = 2,
                    title = if (english) "ESP32 DSP Filtered (Notch 50Hz)" else "Lọc trên chip ESP32 (Notch 50Hz)",
                    subtitle = if (english) "Hardware DSP: IIR Notch 50Hz + baseline wander removal"
                    else "Xử lý số tín hiệu trên vi điều khiển ESP32: Lọc Notch 50Hz + chống trôi nền",
                    samples = espFiltered,
                    waveformColor = EcgTheme.colors.espFilteredChannel,
                    isPeak = assessment.bpm != null,
                    height = if (selectedChannelFilter != 0) 240.dp else 195.dp
                )
            }

            // Channel 3: Filtered on Android Local DSP
            if (selectedChannelFilter == 0 || selectedChannelFilter == 3) {
                EcgChartCard(
                    channelNumber = 3,
                    title = if (english) "Android Local Filtered (DSP)" else "Lọc trên máy Android (Kiểm chứng)",
                    subtitle = if (english) "Computed in Kotlin for realtime filter validation"
                    else "Xử lý bằng bộ lọc RealtimeEcgFilter cục bộ để đối chiếu chất lượng",
                    samples = androidFiltered,
                    waveformColor = EcgTheme.colors.androidFilteredChannel,
                    height = if (selectedChannelFilter != 0) 240.dp else 195.dp
                )
            }
        }

        // 5. Telemetry Snapshot Summary Card with Monospaced Numerals
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            fontFamily = FontFamily.Monospace,
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
                            fontFamily = FontFamily.Monospace,
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
                            fontFamily = FontFamily.Monospace,
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
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                Text(
                    text = "AI Class: ${aiClassName ?: "--"}  •  125 Hz Sampling  •  ${samples} total samples",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        // 6. Action Buttons: Save to DB & Export CSV
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
