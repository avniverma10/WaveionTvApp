package com.panmetro.iptv.view.uicomponent.error

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.panmetro.iptv.R

@Composable
fun BlockUserScreen(
    showDialog: Boolean,
    title: String? = null,
    message: String? = null,
    confirmButtonText: String? = "Exit",
    onConfirm: (() -> Unit)? = null,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    if (!showDialog) return

    // Full screen blurred background that blocks interactions
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .blur(8.dp) // Apply blur effect
    ) {
        BlockedUserDialog(
            showDialog = showDialog,
            title = title,
            message = message ?: "Temporarily blocked. Please contact your provider to continue.",
            painter = painterResource(id = R.drawable.media_error),
            confirmButtonText = confirmButtonText ?: "Exit",
            onConfirm = onConfirm,
            dismissButtonText = dismissButtonText,
            onDismiss = onDismiss,
            borderColor = Color.Transparent
        )
    }
}

@Composable
fun BlockedUserDialog(
    isBlockedUser: Boolean? = false,
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

    Dialog(
        onDismissRequest = { /* Prevent dismiss on outside click */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        // Pre-compute shape and optional border stroke
        val dialogShape = RoundedCornerShape(24.dp)
        val borderStroke = borderColor
            .takeIf { it != Color.Transparent }
            ?.let { BorderStroke(1.dp, it) }

        Surface(
            shape = dialogShape,
            border = borderStroke,
            tonalElevation = 16.dp,
            shadowElevation = 24.dp,
            color = Color(0xFF191B1F),
            modifier = Modifier
                .widthIn(min = 280.dp, max = 400.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─── Icon ───────────────────────────────────────────────
                painter?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ─── Title ──────────────────────────────────────────────
                title?.let { text ->
                    Text(
                        text = text,
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ─── Main message ───────────────────────────────────────
                message?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ─── Error details ──────────────────────────────────────
                if (errorCode != null || errorMessage != null) {
                    Text(
                        text = if (isBlockedUser == false) "${errorMessage.orEmpty()}"
                        else "Error ${errorCode.orEmpty()}: ${errorMessage.orEmpty()}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ─── Buttons ────────────────────────────────────────────
                if (confirmButtonText != null || dismissButtonText != null) {
                    val dismissRequester = remember { FocusRequester() }
                    val dismissInteraction = remember { MutableInteractionSource() }
                    val isDismissFocused by dismissInteraction.collectIsFocusedAsState()

                    val confirmRequester = remember { FocusRequester() }
                    val confirmInteraction = remember { MutableInteractionSource() }
                    val isConfirmFocused by confirmInteraction.collectIsFocusedAsState()

                    LaunchedEffect(showDialog) {
                        if (initialFocusOnConfirm && confirmButtonText != null) {
                            try {
                                confirmRequester.requestFocus()
                            } catch (e: IllegalStateException) { }
                        } else if (dismissButtonText != null) {
                            try {
                            dismissRequester.requestFocus()
                            } catch (e: IllegalStateException) { }
                        } else if (confirmButtonText != null) {
                            try {
                                confirmRequester.requestFocus()
                            } catch (e: IllegalStateException) { }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Dismiss button
                        if (dismissButtonText != null && onDismiss != null) {
                            TextButton(
                                onClick = { onDismiss() },
                                interactionSource = dismissInteraction,
                                modifier = Modifier
                                    .focusRequester(dismissRequester)
                                    .focusable(interactionSource = dismissInteraction)
                                    .border(
                                        BorderStroke(
                                            width = 1.dp,
                                            color = if (isDismissFocused) Color(0xFF49FEDD) else Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isDismissFocused) Color(0x1A49FEDD) else Color(0xFF414857),
                                    contentColor = if (isDismissFocused) Color.White else Color.White
                                )
                            ) {
                                Text(dismissButtonText, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                        }

                        // Confirm button
                        if (confirmButtonText != null && onConfirm != null) {
                            TextButton(
                                onClick = { onConfirm() },
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
                                    .defaultMinSize(minWidth = 100.dp, minHeight = 48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isConfirmFocused) Color(0x1A49FEDD) else Color(0xFF49FEDD),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(confirmButtonText, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Int?.orEmpty(): String = this?.toString() ?: ""