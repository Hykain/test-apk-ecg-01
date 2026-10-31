package com.example.ecgmonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.data.EcgSession
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Material 3 7-Day Heart Rate Trends & Session History Screen.
 * Features:
 * - Interactive 7-Day Bézier Spline Chart with shaded clinical reference band (60-100 BPM)
 * - Weekly summary statistics KPIs (Average, Min-Max Range, Total Sessions)
 * - Medical assessment banner with actionable health feedback
 * - Comprehensive saved session history with instant CSV export
 */
@Composable
fun TrendsScreen(
    sessions: List<EcgSession>,
    onExportSessionCsv: (EcgSession) -> Unit,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    val formatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    val dates = remember {
        (0..6).map { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -it) }.time }.reversed()
    }
    val byDay = sessions.groupBy { formatter.format(Date(it.startedAt)) }
    val values = dates.map { byDay[formatter.format(it)]?.mapNotNull { s -> s.heartRateBpm }?.average()?.toFloat() }
    val recorded = values.filterNotNull()

    val avgBpm = if (recorded.isNotEmpty()) recorded.average().toInt() else null
    val minBpm = if (recorded.isNotEmpty()) recorded.minOrNull()?.toInt() else null
    val maxBpm = if (recorded.isNotEmpty()) recorded.maxOrNull()?.toInt() else null

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Heart Rate Variability (HRV / SDNN) Estimation from recorded sessions
    val bpmList = sessions.mapNotNull { it.heartRateBpm }.filter { it in 40..200 }
    val estimatedSdnn = if (bpmList.size >= 2) {
        val rrList = bpmList.map { 60000.0 / it }
        val avgRr = rrList.average()
        val variance = rrList.map { (it - avgRr) * (it - avgRr) }.average()
        sqrt(variance).toInt().coerceIn(15, 120)
    } else null

    val hrvStatus = when {
        estimatedSdnn == null -> if (english) "Need >= 2 sessions" else "Cần >= 2 phiên đo"
        estimatedSdnn >= 50 -> if (english) "Optimal Recovery" else "Phục hồi tối ưu"
        estimatedSdnn >= 30 -> if (english) "Moderate Balance" else "Cân bằng"
        else -> if (english) "Elevated Cardiac Strain" else "Căng thẳng tim mạch"
    }
    val hrvColor = when {
        estimatedSdnn == null -> MaterialTheme.colorScheme.onSurfaceVariant
        estimatedSdnn >= 50 -> EcgTheme.colors.rhythmNormal
        estimatedSdnn >= 30 -> EcgTheme.colors.androidFilteredChannel
        else -> EcgTheme.colors.rhythmPvc
    }

    val comment = when {
        recorded.isEmpty() -> if (english) {
            "No measurements in the last 7 days. Measure daily to establish your baseline trend."
        } else {
            "Chưa có phiên đo trong 7 ngày gần đây. Hãy đo mỗi ngày vào cùng khung giờ để theo dõi xu hướng sức khoẻ tim mạch."
        }
        recorded.any { it < 50 || it > 100 } -> if (english) {
            "Some recorded daily averages are outside reference resting range. Check symptoms and consult a doctor if needed."
        } else {
            "Có ngày ghi nhận nhịp tim trung bình ngoài khoảng tham khảo (50 - 100 BPM). Hãy theo dõi triệu chứng và tham khảo ý kiến bác sĩ khi cần."
        }
        else -> if (english) {
            "Recorded daily averages are within resting reference range. Keep up regular wellness tracking."
        } else {
            "Các ngày đã đo đều có nhịp tim trung bình trong khoảng tham khảo chuẩn. Hãy tiếp tục duy trì đo đạc đều đặn mỗi ngày."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1 Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (english) "7-Day Heart Rate Trends" else "Xu hướng Nhịp tim 7 Ngày",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 3-Metric Weekly KPI Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Weekly Average
            WeeklyKpiTile(
                title = if (english) "Weekly Avg" else "Trung bình",
                value = avgBpm?.let { "$it" } ?: "--",
                unit = "BPM",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            // Min-Max Range
            val rangeText = if (minBpm != null && maxBpm != null) "$minBpm - $maxBpm" else "--"
            WeeklyKpiTile(
                title = if (english) "Min - Max" else "Dao động",
                value = rangeText,
                unit = "BPM",
                color = EcgTheme.colors.ppgBpmColor,
                modifier = Modifier.weight(1.3f)
            )
            // Total Sessions Recorded
            WeeklyKpiTile(
                title = if (english) "Sessions" else "Phiên đo",
                value = "${sessions.size}",
                unit = if (english) "total" else "lần",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Assessment Banner Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (english) "Weekly Health Assessment" else "Nhận xét Sức khoẻ Tuần",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Spline Bézier Curve Trend Chart Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (english) "RESTING HEART RATE CURVE" else "ĐƯỜNG CONG NHỊP TIM LÚC NGHỈ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(EcgTheme.colors.rhythmNormal.copy(alpha = 0.4f), CircleShape)
                        )
                        Text(
                            text = if (english) "Safe: 60-100 BPM" else "Khoảng chuẩn: 60-100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Touch Scrubber Inspection Popover Banner
                AnimatedVisibility(visible = selectedPointIndex != null) {
                    val selIdx = selectedPointIndex ?: 0
                    val selDate = dates.getOrNull(selIdx) ?: Date()
                    val selBpm = values.getOrNull(selIdx)
                    val dayFormat = remember { SimpleDateFormat("EEEE, dd/MM", if (english) Locale.US else Locale("vi")) }
                    val formattedDay = dayFormat.format(selDate).replaceFirstChar { it.uppercase() }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formattedDay,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selBpm != null) {
                                        if (selBpm in 60f..100f) (if (english) "Optimal resting rhythm" else "Nhịp nghỉ chuẩn tối ưu")
                                        else if (selBpm < 60f) (if (english) "Low resting rhythm" else "Nhịp nghỉ chậm")
                                        else (if (english) "Elevated resting rhythm" else "Nhịp nghỉ nhanh")
                                    } else {
                                        if (english) "No measurement recorded" else "Chưa ghi nhận phiên đo"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = selBpm?.let { "${it.toInt()}" } ?: "--",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " BPM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 7-Day Bézier Chart Canvas with Touch Scrubber
                SevenDayTrendCanvas(
                    dates = dates,
                    values = values,
                    dateLabels = dates.map { formatter.format(it) },
                    selectedPointIndex = selectedPointIndex,
                    onSelectIndex = { selectedPointIndex = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                // 7-Day Day-by-Day Interactive Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dayAbbrFormatter = remember { SimpleDateFormat("EEE", if (english) Locale.US else Locale("vi")) }
                    val dayNumFormatter = remember { SimpleDateFormat("dd", Locale.getDefault()) }

                    dates.forEachIndexed { idx, date ->
                        val dayBpm = values.getOrNull(idx)
                        val isSelected = selectedPointIndex == idx
                        val statusColor = when {
                            dayBpm == null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            dayBpm in 60f..100f -> EcgTheme.colors.rhythmNormal
                            dayBpm < 60f -> EcgTheme.colors.rhythmSvc
                            else -> EcgTheme.colors.rhythmPvc
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPointIndex = if (selectedPointIndex == idx) null else idx
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = if (isSelected) BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = dayAbbrFormatter.format(date).uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dayNumFormatter.format(date),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Text(
                                    text = dayBpm?.let { "${it.toInt()}" } ?: "--",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = if (dayBpm != null) statusColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Autonomic Heart Rate Variability (HRV / SDNN) Estimation Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
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
                        color = hrvColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = hrvColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (english) "AUTONOMIC HRV BALANCE" else "BIẾN THIÊN NHỊP TIM (HRV)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = hrvStatus,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = hrvColor
                        )
                        Text(
                            text = if (english) "Estimated SDNN recovery index"
                            else "Độ lệch chuẩn khoảng RR (Chỉ số phục hồi tim mạch)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = estimatedSdnn?.toString() ?: "--",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hrvColor,
                            fontSize = 22.sp
                        )
                        Text(
                            text = " ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }

        // Section 2 Header: Saved Sessions History
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (english) "Saved Measurement History" else "Lịch sử Phiên đo & Xuất .CSV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (sessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (english) "No saved sessions yet" else "Chưa có phiên đo nào được lưu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (english) "Record heart signals in ECG Charts and tap 'Save to DB' to store sessions locally."
                        else "Hãy chuyển sang tab Đồ thị ECG và bấm 'Lưu CSDL' để lưu trữ lịch sử phiên đo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sessions.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
                                    Text(
                                        text = fullDateFormatter.format(Date(session.startedAt)),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${session.durationSeconds}s",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Nhịp tim: ${session.heartRateBpm ?: "--"} BPM  •  125 Hz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = EcgTheme.colors.peakMarker
                                )

                                if (session.note.isNotBlank()) {
                                    Text(
                                        text = session.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { onExportSessionCsv(session) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (english) "CSV" else "Xuất CSV")
                            }
                        }
                    }
                }
            }
        }

        // Disclaimer Note
        Text(
            text = if (english) "Trend analysis is for personal wellness tracking only and is not intended for clinical diagnosis."
            else "Theo dõi xu hướng chỉ mang tính tham khảo sức khoẻ cá nhân; không thay thế chỉ định y khoa chuyên sâu.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Weekly Summary Statistic KPI Tile.
 */
@Composable
fun WeeklyKpiTile(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Interactive 7-Day Bézier Spline Chart Canvas with Touch Scrubber.
 */
@Composable
fun SevenDayTrendCanvas(
    dates: List<Date>,
    values: List<Float?>,
    dateLabels: List<String>,
    selectedPointIndex: Int? = null,
    onSelectIndex: ((Int?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val safeBandColor = EcgTheme.colors.rhythmNormal.copy(alpha = 0.08f)
    val safeLineColor = EcgTheme.colors.rhythmNormal.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width
                        val leftMargin = 32.dp.toPx()
                        val rightMargin = 16.dp.toPx()
                        val chartWidth = width - leftMargin - rightMargin
                        val count = values.size
                        if (count > 1 && onSelectIndex != null) {
                            val touchX = offset.x
                            var closestIdx = 0
                            var minDiff = Float.MAX_VALUE
                            for (i in 0 until count) {
                                val x = leftMargin + (i.toFloat() / (count - 1)) * chartWidth
                                val diff = abs(x - touchX)
                                if (diff < minDiff) {
                                    minDiff = diff
                                    closestIdx = i
                                }
                            }
                            onSelectIndex(if (selectedPointIndex == closestIdx) null else closestIdx)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width
                        val leftMargin = 32.dp.toPx()
                        val rightMargin = 16.dp.toPx()
                        val chartWidth = width - leftMargin - rightMargin
                        val count = values.size
                        if (count > 1 && onSelectIndex != null) {
                            val touchX = offset.x
                            var closestIdx = 0
                            var minDiff = Float.MAX_VALUE
                            for (i in 0 until count) {
                                val x = leftMargin + (i.toFloat() / (count - 1)) * chartWidth
                                val diff = abs(x - touchX)
                                if (diff < minDiff) {
                                    minDiff = diff
                                    closestIdx = i
                                }
                            }
                            onSelectIndex(closestIdx)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val width = size.width
                        val leftMargin = 32.dp.toPx()
                        val rightMargin = 16.dp.toPx()
                        val chartWidth = width - leftMargin - rightMargin
                        val count = values.size
                        if (count > 1 && onSelectIndex != null) {
                            val touchX = change.position.x
                            var closestIdx = 0
                            var minDiff = Float.MAX_VALUE
                            for (i in 0 until count) {
                                val x = leftMargin + (i.toFloat() / (count - 1)) * chartWidth
                                val diff = abs(x - touchX)
                                if (diff < minDiff) {
                                    minDiff = diff
                                    closestIdx = i
                                }
                            }
                            onSelectIndex(closestIdx)
                        }
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val leftMargin = 32.dp.toPx()
        val rightMargin = 16.dp.toPx()
        val bottomMargin = 22.dp.toPx()
        val topMargin = 16.dp.toPx()

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin

        val minY = 40f
        val maxY = 120f
        val ySpan = maxY - minY

        fun bpmToY(bpm: Float): Float {
            val norm = (bpm - minY) / ySpan
            return topMargin + (chartHeight * (1f - norm.coerceIn(0f, 1f)))
        }

        // 1. Shaded Safe Reference Band (60 - 100 BPM)
        val y60 = bpmToY(60f)
        val y100 = bpmToY(100f)

        drawRect(
            color = safeBandColor,
            topLeft = Offset(leftMargin, y100),
            size = androidx.compose.ui.geometry.Size(chartWidth, y60 - y100)
        )

        // Dashed lines for 60 & 100 BPM bounds
        drawLine(
            color = safeLineColor,
            start = Offset(leftMargin, y100),
            end = Offset(leftMargin + chartWidth, y100),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
        drawLine(
            color = safeLineColor,
            start = Offset(leftMargin, y60),
            end = Offset(leftMargin + chartWidth, y60),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        // Y-axis Labels (100, 80, 60)
        listOf(100f, 80f, 60f).forEach { bpmTick ->
            val tickY = bpmToY(bpmTick)
            val layout = textMeasurer.measure(
                text = "${bpmTick.toInt()}",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(4.dp.toPx(), tickY - (layout.size.height / 2f))
            )
        }

        // 2. Data Points Calculation
        val validPoints = mutableListOf<Pair<Int, Offset>>()
        val count = values.size

        for (i in 0 until count) {
            val x = leftMargin + (i.toFloat() / (count - 1)) * chartWidth
            val v = values[i]
            if (v != null) {
                validPoints.add(Pair(i, Offset(x, bpmToY(v))))
            }

            // Draw X-axis date labels
            val dateLabel = dateLabels.getOrElse(i) { "" }
            val isSelectedDate = selectedPointIndex == i
            val labelLayout = textMeasurer.measure(
                text = dateLabel,
                style = TextStyle(
                    fontSize = if (isSelectedDate) 10.sp else 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isSelectedDate) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelectedDate) primaryColor else Color.Gray
                )
            )
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(x - (labelLayout.size.width / 2f), height - bottomMargin + 4.dp.toPx())
            )
        }

        // 3. Draw Bézier Spline Curve if we have at least 2 points
        if (validPoints.size >= 2) {
            val splinePath = Path()
            val fillPath = Path()

            val firstPt = validPoints.first().second
            splinePath.moveTo(firstPt.x, firstPt.y)
            fillPath.moveTo(firstPt.x, height - bottomMargin)
            fillPath.lineTo(firstPt.x, firstPt.y)

            for (k in 0 until validPoints.size - 1) {
                val p0 = validPoints[k].second
                val p1 = validPoints[k + 1].second
                val controlX = (p0.x + p1.x) / 2f

                splinePath.cubicTo(
                    controlX, p0.y,
                    controlX, p1.y,
                    p1.x, p1.y
                )
                fillPath.cubicTo(
                    controlX, p0.y,
                    controlX, p1.y,
                    p1.x, p1.y
                )
            }

            val lastPt = validPoints.last().second
            fillPath.lineTo(lastPt.x, height - bottomMargin)
            fillPath.close()

            // Area Gradient Fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                    startY = topMargin,
                    endY = height - bottomMargin
                )
            )

            // Crisp Bézier Line
            drawPath(
                path = splinePath,
                color = primaryColor,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 4. Draw Circular Nodes
        validPoints.forEach { (idx, pt) ->
            val isSelected = selectedPointIndex == idx
            // Outer node glow
            drawCircle(
                color = primaryColor.copy(alpha = if (isSelected) 0.45f else 0.25f),
                radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                center = pt
            )
            // Node border
            drawCircle(
                color = primaryColor,
                radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
                center = pt
            )
            // Node center white dot
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pt
            )
        }

        // 5. Vertical Touch Scrubber Guideline & Active Inspection Ring
        if (selectedPointIndex != null && selectedPointIndex in 0 until count) {
            val scrubX = leftMargin + (selectedPointIndex.toFloat() / (count - 1)) * chartWidth
            drawLine(
                color = primaryColor.copy(alpha = 0.65f),
                start = Offset(scrubX, topMargin),
                end = Offset(scrubX, height - bottomMargin),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
            )

            values.getOrNull(selectedPointIndex)?.let { valBpm ->
                val scrubY = bpmToY(valBpm)
                // Concentric inspection target rings
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = 14.dp.toPx(),
                    center = Offset(scrubX, scrubY)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(scrubX, scrubY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(scrubX, scrubY)
                )
            }
        }
    }
}
