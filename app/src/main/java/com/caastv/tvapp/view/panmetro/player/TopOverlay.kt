package com.caastv.tvapp.view.panmetro.player


import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import com.android.caastv.R
import com.caastv.tvapp.utils.theme.base_color

@Composable
fun VideoPlayerWithTopOverlay(
    focusRequester: FocusRequester,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onAudioClick: () -> Unit = {},
    onSubtitlesClick: () -> Unit = {},
    onHdClick : () -> Unit = {},
    qualityLabel: String,
    subtitleButtonFocusRequester: FocusRequester,
    audioButtonFocusRequester: FocusRequester,
    videoButtonFocusRequester: FocusRequester,
    onDismiss: () -> Unit

) {

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .focusRequester(focusRequester)
            .animateContentSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OverlayButton(
                iconRes = if (isFavorite) R.drawable.filled_heart else R.drawable.heart,
                label   = "Favorites",
                onClick = {
                    onFavoriteClick()
                },
                focusRequester = focusRequester
            )
            Spacer(Modifier.width(24.dp))

            OverlayButton(
                iconRes = R.drawable.audio,
                label = "Audio",
                onClick = onAudioClick,
                focusRequester = audioButtonFocusRequester
            )
            Spacer(Modifier.width(24.dp))

            OverlayButton(
                iconRes = R.drawable.subtitles,
                label = "Subtitles",
                onClick = onSubtitlesClick,
                focusRequester = subtitleButtonFocusRequester

            )

            Spacer(Modifier.width(24.dp))

            OverlayButton(
                iconRes = R.drawable.video_quality,
                label =  qualityLabel,
                onClick = onHdClick,
                focusRequester = videoButtonFocusRequester
            )
        }
    }
}

@Composable
private fun OverlayButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    focusRequester: FocusRequester
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .then(
                if (isFocused)
                    Modifier.border(2.dp, base_color, RoundedCornerShape(30.dp))
                else Modifier
            )
            .background(if (isFocused) base_color.copy(alpha = 0.2f) else Color.Transparent)
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}
