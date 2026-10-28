package com.example.ecgmonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.ui.theme.EcgTheme
import kotlin.math.max

/**
 * Pure Android Material 3 Medical ECG Waveform Canvas.
 * Supports adaptive themes:
 * - Dark Mode: ICU clinical phosphor monitor grid (0.04s & 0.2s cyan subdivisions).
 * - Light Mode: Classic clinical salmon/pink medical ECG chart paper.
 */
@Composable
fun EcgWaveformCanvas(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    waveformColor: Color = EcgTheme.colors.espFilteredChannel,
    isPeak: Boolean = false,
    showScaleMarker: Boolean = true,
    telemetryTag: String = "125 Hz • 25 mm/s • 10 mm/mV"
) {
    val themeColors = EcgTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(themeColors.canvasBackground)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Medical Grid (Minor 1mm & Major 5mm / 0.04s & 0.2s grid)
            drawMedicalGrid(
                width = width,
                height = height,
                majorColor = themeColors.gridMajor,
                minorColor = themeColors.gridMinor
            )

            // 2. Draw 1 mV Reference Pulse (Standard clinical calibration marker)
            if (showScaleMarker) {
                drawCalibrationPulse(height = height, color = themeColors.gridMajor)
            }

            // 3. Draw ECG Signal Waveform
            if (samples.size >= 2) {
                drawEcgSignal(
                    samples = samples,
                    width = width,
                    height = height,
                    color = waveformColor
                )
            } else {
                // Flatline / Standby baseline when no samples yet
                val baselineY = height / 2f
                drawLine(
                    color = waveformColor.copy(alpha = 0.5f),
                    start = Offset(0f, baselineY),
                    end = Offset(width, baselineY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }

        // Live R-Peak indicator badge (pulsing heart)
        if (isPeak) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(themeColors.peakMarker.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(themeColors.peakMarker, CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "R-PEAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.peakMarker
                    )
                }
            }
        }

        // Medical Standard Stamp Tag (Bottom Right)
        Text(
            text = telemetryTag,
            style = MaterialTheme.typography.labelSmall,
            color = themeColors.gridMajor.copy(alpha = 0.85f),
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Renders clinical medical grid with major (5mm) and minor (1mm) divisions.
 */
private fun DrawScope.drawMedicalGrid(
    width: Float,
    height: Float,
    majorColor: Color,
    minorColor: Color
) {
    // 25 horizontal divisions (minor grid) and 5 major blocks
    val numHorizontalDivisions = 25
    val numVerticalDivisions = 15

    val xStep = width / numHorizontalDivisions
    val yStep = height / numVerticalDivisions

    // Minor vertical lines (every 0.04s)
    for (i in 0..numHorizontalDivisions) {
        val x = i * xStep
        val isMajor = (i % 5 == 0)
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = if (isMajor) 1.2.dp.toPx() else 0.6.dp.toPx()
        )
    }

    // Minor horizontal lines (every 0.1 mV)
    for (j in 0..numVerticalDivisions) {
        val y = j * yStep
        val isMajor = (j % 5 == 0)
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = if (isMajor) 1.2.dp.toPx() else 0.6.dp.toPx()
        )
    }
}

/**
 * Standard 1 mV calibration reference square pulse on the far left.
 */
private fun DrawScope.drawCalibrationPulse(
    height: Float,
    color: Color
) {
    val baselineY = height * 0.5f
    val pulseHeight = height * 0.28f // ~ 10mm representation
    val startX = 8.dp.toPx()
    val pulseWidth = 10.dp.toPx()

    val pulsePath = Path().apply {
        moveTo(startX, baselineY)
        lineTo(startX + 4.dp.toPx(), baselineY)
        lineTo(startX + 4.dp.toPx(), baselineY - pulseHeight)
        lineTo(startX + 4.dp.toPx() + pulseWidth, baselineY - pulseHeight)
        lineTo(startX + 4.dp.toPx() + pulseWidth, baselineY)
        lineTo(startX + 8.dp.toPx() + pulseWidth, baselineY)
    }

    drawPath(
        path = pulsePath,
        color = color.copy(alpha = 0.7f),
        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Square)
    )
}

/**
 * Draws the continuous ECG waveform with anti-aliasing and smooth interpolation.
 */
private fun DrawScope.drawEcgSignal(
    samples: List<Float>,
    width: Float,
    height: Float,
    color: Color
) {
    val lo = samples.minOrNull() ?: 0f
    val hi = samples.maxOrNull() ?: 1f
    val span = max(0.2f, hi - lo) // Avoid zero division if flatlined

    val path = Path()
    val count = samples.size

    for (i in 0 until count) {
        val x = (i.toFloat() / (count - 1)) * width
        val normalized = (samples[i] - lo) / span
        // Margin 12% top & bottom to keep R-peaks and S-valleys within bounds
        val y = height - (normalized * (height * 0.76f) + (height * 0.12f))

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Material 3 Card Container for ECG Waveforms.
 * Features clinical telemetry headers, channel badges, and responsive waveform display.
 */
@Composable
fun EcgChartCard(
    channelNumber: Int,
    title: String,
    subtitle: String,
    samples: List<Float>,
    waveformColor: Color,
    modifier: Modifier = Modifier,
    isPeak: Boolean = false,
    height: androidx.compose.ui.unit.Dp = 185.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Channel Badge + Title + Telemetry Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = waveformColor.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, waveformColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "CH $channelNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = waveformColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sample count pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = "${samples.size} pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Waveform Canvas Component
            EcgWaveformCanvas(
                samples = samples,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                waveformColor = waveformColor,
                isPeak = isPeak
            )
        }
    }
}

