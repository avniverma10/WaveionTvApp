package com.caastv.tvapp.view.panmetro.player

import android.view.KeyEvent
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caastv.tvapp.utils.theme.base_color
import kotlinx.coroutines.delay

@Composable
fun SelectionOverlay(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    title: String,
    options: List<String>,
    selected: String?,
    extraTopOption: String? = null,
    onExtraTopSelect: (() -> Unit)? = null,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val totalRows = options.size + if (extraTopOption != null) 1 else 0
    val initialSelectedIndex = remember(selected, options, extraTopOption) {
        when {
            selected == null && extraTopOption != null -> 0
            selected == null -> 0
            else -> {
                val index = options.indexOf(selected)
                if (index >= 0) index + if (extraTopOption != null) 1 else 0 else 0
            }
        }
    }
    var selectedIndex by remember { mutableStateOf(initialSelectedIndex) }
    val modifierWithFocus = modifier.focusRequester(focusRequester).focusable()

    val rowRequesters = remember(totalRows) {
        List(totalRows) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        delay(50)
        rowRequesters[selectedIndex].requestFocus()
    }

    Box(
        modifier = modifierWithFocus
            .fillMaxSize()
            .onPreviewKeyEvent { ev ->
                if (ev.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (ev.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                        rowRequesters[selectedIndex].requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        selectedIndex = (selectedIndex + 1).coerceAtMost(totalRows - 1)
                        rowRequesters[selectedIndex].requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER -> {
                        if (extraTopOption != null && selectedIndex == 0) {
                            onExtraTopSelect?.invoke()
                        } else {
                            val optionIndex = if (extraTopOption != null) selectedIndex - 1 else selectedIndex
                            onSelect(options[optionIndex])
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                    true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        true
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 76.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .width(250.dp)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            androidx.compose.foundation.lazy.LazyColumn {
                extraTopOption?.let { label ->
                    item {
                        OptionRow(
                            text = label,
                            isSelected = selectedIndex == 0,
                            focusRequester = rowRequesters[0]
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(options.size) { idx ->
                    val rowIdx = if (extraTopOption != null) idx + 1 else idx
                    OptionRow(
                        text = options[idx],
                        isSelected = selectedIndex == rowIdx,
                        focusRequester = rowRequesters[rowIdx]
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
private fun OptionRow(
    text: String,
    isSelected: Boolean,
    focusRequester: FocusRequester
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = if (isSelected) Color(0x1A49FEDD) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused) base_color else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.width(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White)
    }
}
