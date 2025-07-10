import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.caastv.R
import com.example.tvapp.extensions.getFloatValue
import com.example.tvapp.model.data.sseresponse.ForceMessage
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun ForceMessageDialog(
    showDialog: Boolean,
    forceMessage: ForceMessage?,
    onConfirm:() -> Unit
) {
    if (!showDialog) return
    val context = LocalContext.current
    val displayDuration = remember(forceMessage) {
        when {
            forceMessage?.duration == null -> Long.MAX_VALUE  // Show forever if null
            forceMessage?.duration?.toString().getFloatValue() <= 0 -> Long.MAX_VALUE  // Negative or zero duration
            else -> (forceMessage?.duration?.toString().getFloatValue()*1000).toLong()  // Positive duration in ms
        }
    }
    LaunchedEffect(forceMessage) {
        delay(displayDuration)
        onConfirm()
    }

    val forcePush = forceMessage?.forcePush == true
    //Keep a hh:mm:ss clock, updating every second
    val currentTime by produceState(initialValue = formatSecondsToHMS(forceMessage?.duration?:0L).format(Date())) {
        while (true) {
            value = formatSecondsToHMS(forceMessage?.duration?:0L)
            delay(1_000L)
        }
    }

    val borderColor = Color(0xFF49FEDD)

    val titleColor = runCatching {
        Color(android.graphics.Color.parseColor(forceMessage?.titleFontColorHex ?: "#000000"))
            .copy(
                alpha = forceMessage?.titleFontTransparency
                    ?.getFloatValue()
                    ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.5f)
    }.getOrDefault(Color.Black.copy(alpha = 0.5f))
    val messageColor = runCatching {
        Color(android.graphics.Color.parseColor(forceMessage?.messageFontColorHex ?: "#000000"))
            .copy(
                alpha = forceMessage?.messageFontTransparency
                    ?.getFloatValue()
                    ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.5f)
    }.getOrDefault(Color.Black.copy(alpha = 0.5f))

    val bgColor = runCatching {
        Color(
            android.graphics.Color.parseColor(
                forceMessage?.messageBackgroundColorHex ?: "#ffffff"
            )
        )
            .copy(
                alpha = forceMessage?.messageBackgroundTransparency
                    ?.getFloatValue()
                    ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.5f)
    }.getOrDefault(Color.White.copy(alpha = 0.25f))
    // We only steal *all* keys/touches if forcePush==true
    val blockerModifier = if (forcePush) {
        Modifier
            .fillMaxSize()
            .focusable()             // grab focus
            .onPreviewKeyEvent { true } // swallow every key
            .pointerInput(Unit) {       // swallow every touch
                awaitPointerEventScope { while(true) awaitPointerEvent() }
            }
    } else {
        Modifier  // no blocking behavior
    }



    val dialogProps = DialogProperties(
        dismissOnBackPress    = !forcePush,
        dismissOnClickOutside = !forcePush
    )

    Dialog(
        onDismissRequest = { if (!forcePush) onConfirm() },
        properties = dialogProps
    ) {
        val focusRequester = remember { FocusRequester() }
        //Fullscreen blocker that eats all input
        Surface(
            modifier = blockerModifier
                .wrapContentSize()
                .shadow(
                    elevation = 15.dp,  // Shadow intensity
                    shape = RoundedCornerShape(24.dp),
                    clip = true,        // Clips shadow to the shape
                    spotColor = Color(0xFF00D2FF).copy(alpha = 0.5f),  // Direct light shadow
                    ambientColor = Color(0xFF3A7BD5).copy(alpha = 0.5f) // Ambient shadow
                ),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 50.dp,  // Material 3 elevation (affects all sides)
            shadowElevation = 50.dp,  // Explicit shadow elevation
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .width(500.dp)
                    .background(bgColor, RoundedCornerShape(16.dp))
                    .padding(vertical = 32.dp, horizontal = 48.dp)
            ) {
                Text(
                    text = forceMessage?.messageTitle.orEmpty(),
                    color = titleColor,
                    fontSize = (forceMessage?.titleFontSizeDp?:20).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.figtree_medium))
                )

                Text(
                    text = forceMessage?.message.orEmpty(),
                    color = messageColor,
                    fontSize = (forceMessage?.messageFontSizeDp?:16).sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily(Font(R.font.figtree_medium))
                )

                Spacer(modifier = Modifier.height(10.dp))
                val modifier = Modifier
                if (!forcePush) {
                    Button(
                        onClick ={  onConfirm() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF272C34),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = modifier
                            .width(180.dp)
                            .height(50.dp)
                            .border(2.dp, Color.Transparent, RoundedCornerShape(8.dp))
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                                        onConfirm()
                                        true
                                    } else false
                                } else false
                            }
                            .focusable()
                    ) {
                        Text("OK", fontSize = 18.sp)
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            try {
                focusRequester.requestFocus()
            }catch (e: Exception){

            }
        }
    }
}



fun formatSecondsToHMS(totalSeconds: Long): String {
    val hours   = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}