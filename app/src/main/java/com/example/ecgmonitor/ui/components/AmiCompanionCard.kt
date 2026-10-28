package com.example.ecgmonitor.ui.components

import android.content.Context
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Movie
import android.os.SystemClock
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Material 3 Ami Mascot Companion Card.
 * Combines empathetic medical guidance with dynamic animated GIF expressions.
 */
@Composable
fun AmiCompanionCard(
    advice: String,
    gifAsset: String,
    modifier: Modifier = Modifier,
    english: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Advice content & badge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = if (english) "AMI'S INSIGHT" else "LỜI KHUYÊN TỪ AMI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = advice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 20.sp
                )
            }

            // Right Column: Mascot Animated GIF with rounded badge frame
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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

