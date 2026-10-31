package com.example.ecgmonitor.ui.components

import android.content.Context
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Movie
import android.os.SystemClock
import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ecgmonitor.ui.theme.EcgTheme

/**
 * Material 3 Interactive Ami Mascot Companion Card.
 * Combines an empathetic speech-bubble advice container with dynamic animated GIF expressions.
 */
@Composable
fun AmiCompanionCard(
    advice: String,
    gifAsset: String,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    // Subtle breathing glow around Ami's frame
    val infiniteTransition = rememberInfiniteTransition(label = "amiGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amiGlowAlpha"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Speech Bubble Advice Container
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = if (english) "AMI INSIGHT" else "LỜI KHUYÊN TỪ AMI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = advice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }

            // Right Column: Mascot Animated GIF with rounded glowing badge frame
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                GifCompanionView(
                    assetName = gifAsset,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun GifCompanionView(assetName: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx -> GifAssetView(ctx).apply { setAsset(assetName) } },
        update = { view -> view.setAsset(assetName) },
        modifier = modifier
    )
}

private class GifAssetView(context: Context) : View(context) {
    private var currentAssetName = ""
    private var movie: Movie? = null
    private var started = SystemClock.uptimeMillis()

    fun setAsset(assetName: String) {
        if (currentAssetName == assetName && movie != null) return
        currentAssetName = assetName
        movie = try {
            context.assets.open(assetName).use { Movie.decodeStream(it) }
        } catch (_: Exception) {
            try {
                context.assets.open("companion.gif").use { Movie.decodeStream(it) }
            } catch (_: Exception) {
                null
            }
        }
        started = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: AndroidCanvas) {
        val gif = movie ?: return
        val duration = gif.duration().takeIf { it > 0 } ?: 1000
        gif.setTime(((SystemClock.uptimeMillis() - started) % duration).toInt())
        val scale = minOf(
            width.toFloat() / gif.width().coerceAtLeast(1),
            height.toFloat() / gif.height().coerceAtLeast(1)
        )
        canvas.save()
        canvas.translate((width - gif.width() * scale) / 2f, (height - gif.height() * scale) / 2f)
        canvas.scale(scale, scale)
        gif.draw(canvas, 0f, 0f)
        canvas.restore()
        postInvalidateOnAnimation()
    }
}
