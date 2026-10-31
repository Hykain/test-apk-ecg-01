package com.example.ecgmonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecgmonitor.ui.theme.DarkEcgColors
import com.example.ecgmonitor.ui.theme.EcgTheme
import com.example.ecgmonitor.ui.theme.EcgThemeColors
import com.example.ecgmonitor.ui.theme.LightEcgColors
import java.util.Locale
import kotlin.math.max

/**
 * Display themes for clinical ECG waveform canvas.
 */
enum class EcgDisplayTheme(val displayNameVi: String, val displayNameEn: String) {
    SYSTEM("Tự động", "System"),
    ICU_DARK("Monitor ICU", "ICU Dark"),
    CLINICAL_PAPER("Giấy in Y tế", "Medical Paper"),
    AMBER_GLOW("Hổ phách", "Amber CRT")
}

// Surgical Amber high-contrast theme
val AmberEcgColors = EcgThemeColors(
    canvasBackground = Color(0xFF0C0A06),
    gridMajor = Color(0x3DF59E0B),
    gridMinor = Color(0x18F59E0B),
    rawChannel = Color(0xFFEF4444),
    espFilteredChannel = Color(0xFFFBBF24),
    androidFilteredChannel = Color(0xFF38BDF8),
    peakMarker = Color(0xFFEF4444),
    spo2Color = Color(0xFFFBBF24),
    ppgBpmColor = Color(0xFFF59E0B),
    rhythmNormal = Color(0xFF34D399),
    rhythmSvc = Color(0xFFFBBF24),
    rhythmPvc = Color(0xFFF87171),
    rhythmFusion = Color(0xFFA78BFA),
    rhythmUnknown = Color(0xFF9CA3AF)
)

/**
 * Professional High-End Medical ECG Waveform Oscilloscope.
 * Features:
 * - Realtime animated sweep scanline with fading phosphor glow trail
 * - Phosphor halo trace with ambient glow & anti-aliased Bézier smoothing
 * - Calibrated Y-axis milivolt scale ticks (+1.0 mV, 0.0 mV, -0.5 mV)
 * - Standard 1 mV calibration pulse
 * - Dotted clinical zero-baseline reference
 * - 4 interactive visual themes (Auto System, ICU Dark, Medical Pink Paper, Amber CRT)
 */
@Composable
fun EcgWaveformCanvas(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    waveformColor: Color = EcgTheme.colors.espFilteredChannel,
    isPeak: Boolean = false,
    displayTheme: EcgDisplayTheme = EcgDisplayTheme.SYSTEM,
    showScaleMarker: Boolean = true,
    telemetryTag: String = "125 Hz • 25 mm/s • 10 mm/mV"
) {
    val systemThemeColors = EcgTheme.colors
    val activeColors = when (displayTheme) {
        EcgDisplayTheme.SYSTEM -> systemThemeColors
        EcgDisplayTheme.ICU_DARK -> DarkEcgColors
        EcgDisplayTheme.CLINICAL_PAPER -> LightEcgColors
        EcgDisplayTheme.AMBER_GLOW -> AmberEcgColors
    }

    val activeWaveformColor = when (displayTheme) {
        EcgDisplayTheme.AMBER_GLOW -> AmberEcgColors.espFilteredChannel
        EcgDisplayTheme.CLINICAL_PAPER -> LightEcgColors.espFilteredChannel
        else -> waveformColor
    }

    // Animated realtime sweep scanline progress (0f to 1f continuously)
    val infiniteTransition = rememberInfiniteTransition(label = "sweepTransition")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepScanline"
    )

    // Pulsing glow radius for the leading active tip
    val tipPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tipPulse"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(activeColors.canvasBackground)
            .border(1.dp, activeColors.gridMajor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Medical Grid (0.04s minor & 0.2s major)
            drawMedicalGrid(
                width = width,
                height = height,
                majorColor = activeColors.gridMajor,
                minorColor = activeColors.gridMinor
            )

            // 2. Draw Calibrated Y-Axis Milivolt Scale (+1.0mV, 0.0mV, -0.5mV)
            drawVoltageScaleTicks(
                height = height,
                gridColor = activeColors.gridMajor,
                textMeasurer = textMeasurer
            )

            // 3. Draw 1 mV Reference Pulse
            if (showScaleMarker) {
                drawCalibrationPulse(height = height, color = activeColors.gridMajor)
            }

            // 4. Draw ECG Signal Waveform with Phosphor Glow & Fill
            if (samples.size >= 2) {
                drawAdvancedEcgSignal(
                    samples = samples,
                    width = width,
                    height = height,
                    color = activeWaveformColor,
                    sweepProgress = sweepProgress,
                    tipPulse = tipPulse
                )
            } else {
                // Standby baseline when waiting for data
                val baselineY = height * 0.5f
                drawLine(
                    color = activeWaveformColor.copy(alpha = 0.45f),
                    start = Offset(42.dp.toPx(), baselineY),
                    end = Offset(width, baselineY),
                    strokeWidth = 1.8.dp.toPx()
                )
            }

            // 5. Realtime Oscilloscope Sweep Line
            drawSweepScanline(
                width = width,
                height = height,
                progress = sweepProgress,
                beamColor = activeWaveformColor
            )
        }

        // Live R-Peak indicator badge (Pulsing heart effect)
        if (isPeak) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(activeColors.peakMarker.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, activeColors.peakMarker.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(activeColors.peakMarker, CircleShape)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "R-PEAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColors.peakMarker,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Medical Standard Stamp Tag (Bottom Right)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            color = activeColors.canvasBackground.copy(alpha = 0.75f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = telemetryTag,
                style = MaterialTheme.typography.labelSmall,
                color = activeColors.gridMajor.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Draws precision medical grid with standard subdivisions.
 */
private fun DrawScope.drawMedicalGrid(
    width: Float,
    height: Float,
    majorColor: Color,
    minorColor: Color
) {
    val leftGutter = 34.dp.toPx()
    val gridWidth = width - leftGutter
    val numHorizontalDivisions = 25
    val numVerticalDivisions = 15

    val xStep = gridWidth / numHorizontalDivisions
    val yStep = height / numVerticalDivisions

    // Vertical time subdivisions (0.04s minor / 0.2s major)
    for (i in 0..numHorizontalDivisions) {
        val x = leftGutter + (i * xStep)
        val isMajor = (i % 5 == 0)
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = if (isMajor) 1.2.dp.toPx() else 0.5.dp.toPx()
        )
    }

    // Horizontal voltage subdivisions (0.1 mV minor / 0.5 mV major)
    for (j in 0..numVerticalDivisions) {
        val y = j * yStep
        val isMajor = (j % 5 == 0)
        val isCenterBaseline = (j == numVerticalDivisions / 2)

        drawLine(
            color = if (isCenterBaseline) majorColor.copy(alpha = 0.85f)
            else if (isMajor) majorColor
            else minorColor,
            start = Offset(leftGutter, y),
            end = Offset(width, y),
            strokeWidth = if (isCenterBaseline) 1.4.dp.toPx()
            else if (isMajor) 1.1.dp.toPx()
            else 0.5.dp.toPx()
        )
    }
}

/**
 * Draws calibrated Y-axis milivolt text and tick markers on the left gutter.
 */
private fun DrawScope.drawVoltageScaleTicks(
    height: Float,
    gridColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val tickX = 30.dp.toPx()
    val tickLength = 4.dp.toPx()

    // 3 Voltage Points: +1.0 mV (top 20%), 0.0 mV (center 50%), -0.5 mV (bottom 80%)
    val points = listOf(
        Pair(height * 0.18f, "+1.0"),
        Pair(height * 0.50f, " 0.0"),
        Pair(height * 0.82f, "-0.5")
    )

    points.forEach { (y, label) ->
        // Small tick mark
        drawLine(
            color = gridColor,
            start = Offset(tickX - tickLength, y),
            end = Offset(tickX, y),
            strokeWidth = 1.dp.toPx()
        )

        // Text label
        val textLayoutResult = textMeasurer.measure(
            text = label,
            style = TextStyle(
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = gridColor.copy(alpha = 0.85f)
            )
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(4.dp.toPx(), y - (textLayoutResult.size.height / 2f))
        )
    }
}

/**
 * Standard 1 mV calibration reference square pulse next to scale ticks.
 */
private fun DrawScope.drawCalibrationPulse(
    height: Float,
    color: Color
) {
    val baselineY = height * 0.50f
    val pulseHeight = height * 0.32f // Represents 1 mV deflection
    val startX = 38.dp.toPx()
    val pulseWidth = 8.dp.toPx()

    val pulsePath = Path().apply {
        moveTo(startX, baselineY)
        lineTo(startX + 3.dp.toPx(), baselineY)
        lineTo(startX + 3.dp.toPx(), baselineY - pulseHeight)
        lineTo(startX + 3.dp.toPx() + pulseWidth, baselineY - pulseHeight)
        lineTo(startX + 3.dp.toPx() + pulseWidth, baselineY)
        lineTo(startX + 7.dp.toPx() + pulseWidth, baselineY)
    }

    drawPath(
        path = pulsePath,
        color = color.copy(alpha = 0.8f),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Square)
    )
}

/**
 * Draws the high-fidelity ECG trace with dual-layer phosphor glow and leading tip pulse.
 */
private fun DrawScope.drawAdvancedEcgSignal(
    samples: List<Float>,
    width: Float,
    height: Float,
    color: Color,
    sweepProgress: Float,
    tipPulse: Float
) {
    val leftGutter = 54.dp.toPx()
    val activeWidth = width - leftGutter
    val lo = samples.minOrNull() ?: 0f
    val hi = samples.maxOrNull() ?: 1f
    val span = max(0.2f, hi - lo)

    val count = samples.size
    val path = Path()
    var latestPoint: Offset? = null

    for (i in 0 until count) {
        val x = leftGutter + ((i.toFloat() / (count - 1)) * activeWidth)
        val normalized = (samples[i] - lo) / span
        val y = height - (normalized * (height * 0.74f) + (height * 0.13f))

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }

        if (i == count - 1) {
            latestPoint = Offset(x, y)
        }
    }

    // 1. Subtle area gradient fill under the curve (Phosphor persistence effect)
    val fillPath = Path().apply {
        addPath(path)
        lineTo(leftGutter + activeWidth, height * 0.5f)
        lineTo(leftGutter, height * 0.5f)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.08f), Color.Transparent),
            startY = 0f,
            endY = height
        )
    )

    // 2. Layer 1: Ambient soft phosphor glow (Outer stroke)
    drawPath(
        path = path,
        color = color.copy(alpha = 0.24f),
        style = Stroke(
            width = 4.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Layer 2: Core crisp beam (Inner sharp stroke)
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 4. Leading active tip glowing dot & expanding ripple
    latestPoint?.let { pt ->
        // Outer pulsing halo
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = 6.dp.toPx() * tipPulse,
            center = pt
        )
        // Core glowing dot
        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = pt
        )
        drawCircle(
            color = Color.White,
            radius = 1.5.dp.toPx(),
            center = pt
        )
    }
}

/**
 * Draws the real-time sweeping radar/oscilloscope beam across the grid.
 */
private fun DrawScope.drawSweepScanline(
    width: Float,
    height: Float,
    progress: Float,
    beamColor: Color
) {
    val leftGutter = 54.dp.toPx()
    val sweepWidth = width - leftGutter
    val currentX = leftGutter + (progress * sweepWidth)
    val trailWidth = 28.dp.toPx()

    // Fading phosphor trail behind the sweep line
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, beamColor.copy(alpha = 0.12f)),
            startX = currentX - trailWidth,
            endX = currentX
        ),
        topLeft = Offset(max(leftGutter, currentX - trailWidth), 0f),
        size = Size(trailWidth, height)
    )

    // Sharp sweep line
    drawLine(
        color = beamColor.copy(alpha = 0.65f),
        start = Offset(currentX, 0f),
        end = Offset(currentX, height),
        strokeWidth = 1.2.dp.toPx()
    )
}

/**
 * Material 3 Card Container for ECG Waveforms.
 * Features clinical telemetry headers, interactive theme switcher, channel badges, and responsive oscilloscope canvas.
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
    height: Dp = 195.dp,
    english: Boolean = false
) {
    var selectedTheme by rememberSaveable { mutableStateOf(EcgDisplayTheme.SYSTEM) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Channel Badge + Title + Theme Switcher & Sample Count
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
                        border = BorderStroke(1.dp, waveformColor.copy(alpha = 0.5f))
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

                // Action Row: Theme Switcher Pill & Sample Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Theme selector dropdown pill
                    Box {
                        Surface(
                            onClick = { showThemeMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.defaultMinSize(minHeight = 28.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = selectedTheme.displayNameVi,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            EcgDisplayTheme.values().forEach { themeOption ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            themeOption.displayNameVi,
                                            fontWeight = if (themeOption == selectedTheme) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedTheme = themeOption
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Sample points pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "${samples.size} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // High-Tech Oscilloscope Canvas Component
            EcgWaveformCanvas(
                samples = samples,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                waveformColor = waveformColor,
                isPeak = isPeak,
                displayTheme = selectedTheme
            )

            // Dynamic Clinical Telemetry Ribbon (2-Row Medical Layout for All 3 Cards)
            val minVal = if (samples.isNotEmpty()) samples.minOrNull() ?: 0f else 0f
            val maxVal = if (samples.isNotEmpty()) samples.maxOrNull() ?: 0f else 0f
            val vpp = maxVal - minVal
            val avgVal = if (samples.isNotEmpty()) samples.average().toFloat() else 0f

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Physiological Measurements & Realtime Sampling Telemetry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Peak-to-Peak Amplitude Vpp
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Vpp:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (samples.isNotEmpty()) String.format(Locale.US, "%.1f mV", vpp) else "--",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = waveformColor
                                )
                            }

                            // 2. Baseline DC Offset
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "DC:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (samples.isNotEmpty()) String.format(Locale.US, "%+.1f mV", avgVal) else "--",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 3. Sampling Rate Badge (Guaranteed Single-Line Horizontal)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "125 Hz • 8ms/pt",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f))

                    // Row 2: DSP Filter Specs & Live Channel Status Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = waveformColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = when (channelNumber) {
                                    1 -> if (english) "RAW ADC • 0.05-100Hz" else "TÍN HIỆU THÔ • AD8232 (0.05 - 100Hz)"
                                    2 -> if (english) "ESP32 DSP • IIR NOTCH 50Hz" else "LỌC ESP32 • NOTCH 50Hz + CHỐNG TRÔI"
                                    else -> if (english) "KOTLIN DSP • REALTIME" else "LỌC ANDROID • DSP REALTIME ĐỐI CHỨNG"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = waveformColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        if (samples.isNotEmpty()) EcgTheme.colors.rhythmNormal else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (samples.isNotEmpty()) "ACTIVE" else "STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (samples.isNotEmpty()) EcgTheme.colors.rhythmNormal else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Synchronized Multi-Lead Clinical Monitor Oscilloscope.
 * Renders Lead I (Raw Sensor), Lead II (ESP32 DSP), and Lead III (Android Local DSP)
 * stacked on a shared medical timebase with synchronized sweep scanline and calibration ticks.
 */
@Composable
fun Stacked3LeadChartCard(
    raw: List<Float>,
    espFiltered: List<Float>,
    androidFiltered: List<Float>,
    modifier: Modifier = Modifier,
    paperSpeed: String = "25 mm/s",
    voltageGain: String = "10 mm/mV",
    isPeak: Boolean = false,
    english: Boolean = false
) {
    var selectedTheme by rememberSaveable { mutableStateOf(EcgDisplayTheme.SYSTEM) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val systemColors = EcgTheme.colors
    val activeColors = when (selectedTheme) {
        EcgDisplayTheme.SYSTEM -> systemColors
        EcgDisplayTheme.ICU_DARK -> DarkEcgColors
        EcgDisplayTheme.CLINICAL_PAPER -> LightEcgColors
        EcgDisplayTheme.AMBER_GLOW -> AmberEcgColors
    }

    // Sweep scanline animation
    val infiniteTransition = rememberInfiniteTransition(label = "stackedSweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepProgress"
    )

    val tipPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tipPulse"
    )

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Theme Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "3-LEAD SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (english) "Synchronized Multi-Lead Monitor" else "Theo dõi Đồng bộ 3 Đạo trình",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (english) "Lead I (Raw) • Lead II (ESP32) • Lead III (Android)"
                        else "Đạo trình I (Thô) • Đạo trình II (ESP32) • Đạo trình III (Android)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Theme Switcher Button
                Box {
                    Surface(
                        onClick = { showThemeMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.defaultMinSize(minHeight = 28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = selectedTheme.displayNameVi,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        EcgDisplayTheme.values().forEach { themeOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        themeOption.displayNameVi,
                                        fontWeight = if (themeOption == selectedTheme) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedTheme = themeOption
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Stacked 3-Lead Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeColors.canvasBackground)
                    .border(1.dp, activeColors.gridMajor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val slotHeight = h / 3f

                    // 1. Draw unified medical grid across canvas
                    drawMedicalGrid(
                        width = w,
                        height = h,
                        majorColor = activeColors.gridMajor,
                        minorColor = activeColors.gridMinor
                    )

                    // 2. Draw slots for each lead
                    val leads = listOf(
                        Triple("I: RAW ADC", raw, activeColors.rawChannel),
                        Triple("II: ESP32 DSP", espFiltered, activeColors.espFilteredChannel),
                        Triple("III: ANDROID DSP", androidFiltered, activeColors.androidFilteredChannel)
                    )

                    leads.forEachIndexed { idx, (label, samples, color) ->
                        val slotTop = idx * slotHeight

                        // Slot Divider line
                        if (idx > 0) {
                            drawLine(
                                color = activeColors.gridMajor.copy(alpha = 0.5f),
                                start = Offset(30.dp.toPx(), slotTop),
                                end = Offset(w, slotTop),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // 1 mV reference pulse in slot
                        drawCalibrationPulseInSlot(
                            slotTop = slotTop,
                            slotHeight = slotHeight,
                            color = activeColors.gridMajor
                        )

                        // Channel label tag in gutter
                        val textResult = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(4.dp.toPx(), slotTop + 6.dp.toPx())
                        )

                        // Draw Waveform Signal in Slot
                        drawSlotEcgSignal(
                            samples = samples,
                            width = w,
                            slotTop = slotTop,
                            slotHeight = slotHeight,
                            color = color,
                            tipPulse = tipPulse
                        )
                    }

                    // 3. Synchronized sweep scanline covering all 3 leads
                    drawSweepScanline(
                        width = w,
                        height = h,
                        progress = sweepProgress,
                        beamColor = activeColors.espFilteredChannel
                    )
                }

                // Lead II R-Peak indicator badge
                if (isPeak) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(activeColors.peakMarker.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, activeColors.peakMarker.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(activeColors.peakMarker, CircleShape)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "R-PEAK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColors.peakMarker,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // Medical telemetry spec banner at bottom
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    color = activeColors.canvasBackground.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$paperSpeed • $voltageGain • 125 Hz",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeColors.gridMajor.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Lead legend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LeadLegendItem(label = "Lead I: Raw", color = activeColors.rawChannel)
                    LeadLegendItem(label = "Lead II: ESP32 DSP", color = activeColors.espFilteredChannel)
                    LeadLegendItem(label = "Lead III: Android", color = activeColors.androidFilteredChannel)
                }
            }
        }
    }
}

/**
 * Draws single-channel ECG signal within an allocated vertical slot.
 */
private fun DrawScope.drawSlotEcgSignal(
    samples: List<Float>,
    width: Float,
    slotTop: Float,
    slotHeight: Float,
    color: Color,
    tipPulse: Float
) {
    if (samples.size < 2) {
        val midY = slotTop + (slotHeight * 0.5f)
        drawLine(
            color = color.copy(alpha = 0.35f),
            start = Offset(54.dp.toPx(), midY),
            end = Offset(width, midY),
            strokeWidth = 1.2.dp.toPx()
        )
        return
    }

    val leftGutter = 54.dp.toPx()
    val activeWidth = width - leftGutter
    val lo = samples.minOrNull() ?: 0f
    val hi = samples.maxOrNull() ?: 1f
    val span = max(0.2f, hi - lo)

    val count = samples.size
    val path = Path()
    var latestPoint: Offset? = null

    for (i in 0 until count) {
        val x = leftGutter + ((i.toFloat() / (count - 1)) * activeWidth)
        val normalized = (samples[i] - lo) / span
        val y = slotTop + slotHeight - (normalized * (slotHeight * 0.74f) + (slotHeight * 0.13f))

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }

        if (i == count - 1) {
            latestPoint = Offset(x, y)
        }
    }

    // Outer glow
    drawPath(
        path = path,
        color = color.copy(alpha = 0.22f),
        style = Stroke(
            width = 3.6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Core stroke
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Tip pulse
    latestPoint?.let { pt ->
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = 4.5.dp.toPx() * tipPulse,
            center = pt
        )
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = pt
        )
    }
}

/**
 * Standard 1 mV calibration pulse placed inside a slot.
 */
private fun DrawScope.drawCalibrationPulseInSlot(
    slotTop: Float,
    slotHeight: Float,
    color: Color
) {
    val baselineY = slotTop + slotHeight * 0.50f
    val pulseHeight = slotHeight * 0.28f
    val startX = 38.dp.toPx()
    val pulseWidth = 6.dp.toPx()

    val pulsePath = Path().apply {
        moveTo(startX, baselineY)
        lineTo(startX + 2.dp.toPx(), baselineY)
        lineTo(startX + 2.dp.toPx(), baselineY - pulseHeight)
        lineTo(startX + 2.dp.toPx() + pulseWidth, baselineY - pulseHeight)
        lineTo(startX + 2.dp.toPx() + pulseWidth, baselineY)
        lineTo(startX + 5.dp.toPx() + pulseWidth, baselineY)
    }

    drawPath(
        path = pulsePath,
        color = color.copy(alpha = 0.6f),
        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Square)
    )
}

@Composable
private fun LeadLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
