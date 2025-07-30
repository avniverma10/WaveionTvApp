package com.caastv.tvapp.view.uicomponent.error

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.android.caastv.R

@Composable
fun CommonDialog(
    isErrorAdded: Boolean?=true,
    showDialog: Boolean,
    title: String? = null,
    message: String? = null,
    painter: Painter? = null,
    errorCode: Int? = null,
    errorMessage: String? = null,
    borderColor: Color = Color.Gray,
    confirmButtonText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    initialFocusOnConfirm: Boolean = false
) {
    if (!showDialog) return

    // Pre-compute shape and optional border stroke
    val dialogShape = RoundedCornerShape(16.dp)
    val borderStroke = borderColor
        .takeIf { it != Color.Transparent }
        ?.let { BorderStroke(1.dp, it) }

    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(
            focusable             = (confirmButtonText != null || dismissButtonText != null),
            dismissOnBackPress    = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = dialogShape,
            border = borderStroke,
            tonalElevation = if (borderStroke != null) 8.dp else 0.dp,
            shadowElevation = 0.dp,
            color = Color(0xFF191B1F),
            modifier = Modifier
                .padding(24.dp)
                .widthIn(min = 200.dp, max = 400.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─── Title + Icon ───────────────────────────────────────────
                title?.let { text ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if(isErrorAdded== true) {
                            Image(
                                painter = painter ?: painterResource(id = R.drawable.error),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = text,
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // ─── Main message ───────────────────────────────────────────
                message?.let {
                    val msgFontSize = if (errorCode == null && errorMessage == null) 19.sp else 16.sp
                    Text(
                        text = it,
                        fontSize = msgFontSize,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = if (confirmButtonText == null && dismissButtonText == null)
                            Modifier.fillMaxWidth() else Modifier
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ─── Error details ──────────────────────────────────────────
                if (errorCode != null || errorMessage != null) {
                    Text(
                        text = if(isErrorAdded == false) "${errorMessage.orEmpty()}" else "Error ${errorCode.orEmpty()}: ${errorMessage.orEmpty()}",
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ─── Buttons ────────────────────────────────────────────────
                if (confirmButtonText != null || dismissButtonText != null) {
                    val dismissRequester   = remember { FocusRequester() }
                    val dismissInteraction = remember { MutableInteractionSource() }
                    val isDismissFocused   by dismissInteraction.collectIsFocusedAsState()

                    val confirmRequester   = remember { FocusRequester() }
                    val confirmInteraction = remember { MutableInteractionSource() }
                    val isConfirmFocused   by confirmInteraction.collectIsFocusedAsState()

                    LaunchedEffect(showDialog) {
                        if (initialFocusOnConfirm && confirmButtonText != null) {
                            confirmRequester.requestFocus()
                        } else if (dismissButtonText != null) {
                            dismissRequester.requestFocus()
                        } else {
                            confirmRequester.requestFocus()
                        }
                    }

                    Spacer(modifier = Modifier.height( if (errorCode == null && errorMessage == null) 30.dp else 0.dp ))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dismiss button
                        if (dismissButtonText != null && onDismiss != null) {
                            TextButton(
                                onClick = onDismiss,
                                interactionSource = dismissInteraction,
                                modifier = Modifier
                                    .focusRequester(dismissRequester)
                                    .focusable(interactionSource = dismissInteraction)
                                    // always reserve the 2.dp border, but toggle its color
                                    .border(
                                        BorderStroke(
                                            width = 1.dp,
                                            color = if (isDismissFocused) Color(0xFF49FEDD) else Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .defaultMinSize(minWidth = 68.dp, minHeight = 44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isDismissFocused) Color(0x1A49FEDD) else Color(0xFF414857),
                                    contentColor = if (isDismissFocused) Color.White else Color.Black
                                )
                            ) {
                                Text(dismissButtonText)
                            }
                            Spacer(Modifier.width(28.dp))
                        }

                        // Confirm button
                        if (confirmButtonText != null && onConfirm != null) {
                            TextButton(
                                onClick = onConfirm,
                                interactionSource = confirmInteraction,
                                modifier = Modifier
                                    .focusRequester(confirmRequester)
                                    .focusable(interactionSource = confirmInteraction)
                                    .border(
                                        BorderStroke(
                                            width = 1.dp,
                                            color = if (isConfirmFocused) Color(0xFF49FEDD) else Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .defaultMinSize(minWidth = 68.dp, minHeight = 44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isConfirmFocused) Color(0x1A49FEDD) else Color(0xFF414857),
                                    contentColor = if (isConfirmFocused) Color.White else Color.Black
                                )
                            ) {
                                Text(confirmButtonText)
                            }
                        }
                    }

                }
            }
        }
    }
}

private fun Int?.orEmpty(): String = this?.toString() ?: ""
