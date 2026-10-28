package com.example.ecgmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.data.EcgSession
import com.example.ecgmonitor.ui.theme.EcgTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Material 3 7-Day Heart Rate Trends & Session History Screen.
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
    val values = dates.map { byDay[formatter.format(it)]?.mapNotNull { s -> s.heartRateBpm }?.average() }
    val recorded = values.filterNotNull()

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
        // Section 1 Header: 7-Day Heart Rate Trends
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

        // Assessment Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Daily Trend Tiles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dates.forEachIndexed { index, date ->
                val bpm = values[index]
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatter.format(date),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = bpm?.let { "${it.toInt()} BPM" } ?: if (english) "No data" else "Chưa đo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                bpm == null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                bpm < 50 || bpm > 100 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
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

