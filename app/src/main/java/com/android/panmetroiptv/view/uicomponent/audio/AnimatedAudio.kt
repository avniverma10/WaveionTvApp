package com.android.panmetroiptv.view.uicomponent.audio
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.android.panmetroiptv.R
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem


@Composable
fun AnimatedAudio(isSongPlaying: Boolean = true, channel: EPGDataItem) {
    var currentRotation by remember {
        mutableFloatStateOf(0f)
    }

    val rotation = remember {
        Animatable(currentRotation)
    }


    LaunchedEffect(isSongPlaying) {
        if (isSongPlaying) {
            rotation.animateTo(
                targetValue = currentRotation + 360f, animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart
                )
            ) {
                currentRotation = if (value >= 360f) 0f else value
            }
        } else {
            if (currentRotation > 0f) {
                rotation.animateTo(
                    targetValue = currentRotation + 50, animationSpec = tween(
                        1250, easing = LinearOutSlowInEasing
                    )
                ) {
                    currentRotation = value
                }
            }
        }
    }

    Animation( channel = channel, rotationDegrees = rotation.value)
}

@Composable
fun Animation(channel: EPGDataItem, rotationDegrees: Float = 0f
) {
    val stops = channel.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.map { Color(android.graphics.Color.parseColor(it.color)) }
        .orEmpty()

    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020))) // fallback
    }

    Box(
        modifier = Modifier
            .aspectRatio(1.0f)
            .clip(roundedShape)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.ModulateAlpha // Force software
                rotationZ = rotationDegrees
            }
    ) {
        // WTV background
        Image(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationDegrees),
            painter = painterResource(id = R.drawable.wtv_background),
            contentDescription = "WTV Background",
        )

        // WTV song cover
        AsyncImage(
            model = channel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.5f)
                .rotate(rotationDegrees)
                .aspectRatio(1.0f)
                .align(Alignment.Center)
                .clip(roundedShape),
                //.background(brush, shape = RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Inside
        )
    }
}