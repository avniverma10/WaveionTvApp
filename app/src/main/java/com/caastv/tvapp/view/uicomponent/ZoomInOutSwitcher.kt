package com.caastv.tvapp.view.uicomponent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.caastv.R
import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ZoomInOutSwitcher(
) {
    // 1) Define your “pages”
    val pages: List<@Composable () -> Unit> = listOf(
        {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "CaasTV Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize() // Stretch the image to fill the inner Box.
                    .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed.
            )

        },
        {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Panmetro Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize() // Stretch the image to fill the inner Box.
                    .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed.
            )
        }
    )

    // 2) State for current page
    var pageIndex by remember { mutableStateOf(0) }

    // 3) Advance every 10s
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            pageIndex = (pageIndex + 1) % pages.size
        }
    }

    // 4) AnimatedContent with zoom in/out
    AnimatedContent(
        targetState = pageIndex,
        transitionSpec = {
            // zoom in from 0.8x to 1x + fadeIn,
            // zoom out from 1x to 0.8x + fadeOut
            (scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(1200, easing = LinearEasing)
            ) + fadeIn(tween(1200))) with
            (scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(1200, easing = LinearEasing)
            ) + fadeOut(tween(1200)))
        }
    ) { target ->
        // Render the current page
        Box(Modifier.fillMaxSize()) {
            pages[target]()
        }
    }
}
