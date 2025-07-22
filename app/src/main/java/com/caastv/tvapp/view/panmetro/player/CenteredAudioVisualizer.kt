package com.caastv.tvapp.view.panmetro.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.SubcomposeAsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size

@Composable
fun CenteredAudioVisualizer(
    isAudio: Boolean,
    brush: Brush,                     // your gradient / solid bg
    modifier: Modifier = Modifier
) {
    if (!isAudio) return

    val context = LocalContext.current

    // Build the request only once per recomposition
    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data("file:///android_asset/waveform_2_1.gif")   // put the file in src/main/assets
            // Needed so the drawable keeps updating every frame ↓
            .decoderFactory(
                GifDecoder.Factory()
                /*if (Build.VERSION.SDK_INT >= 28)
                    ImageDecoderDecoder.Factory()              // AnimatedImageDrawable
                else
                    GifDecoder.Factory()*/                       // GifDrawable
            )
            .allowHardware(false)      // hardware bitmaps can’t animate
            .size(Size.ORIGINAL)       // keep original resolution
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,   // keep bars crisp, no cropping
            modifier = Modifier
                .fillMaxWidth(0.8f)            // 80 % of screen width
                .aspectRatio(1f)               // keep it square
                .clip(MaterialTheme.shapes.medium)
        )
    }
}
