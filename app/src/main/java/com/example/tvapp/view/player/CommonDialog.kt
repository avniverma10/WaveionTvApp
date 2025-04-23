package com.example.tvapp.view.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tvapp.R

/**
 * A reusable common dialog with optional title, message, error details, dynamic border color,
 * and optional confirm/dismiss buttons. Any null parameter will hide its UI element.
 * The icon is always shown next to the title.
 */
@Composable
fun CommonDialog(
    showDialog: Boolean,
    title: String? = null,
    message: String? = null,
    errorCode: Int? = null,
    errorMessage: String? = null,
    borderColor: Color = Color.Gray,
    confirmButtonText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    containerColor: Color = Color(0xFF191B1F),
    titleColor: Color = Color.White,
    contentColor: Color = Color.LightGray
) {
    if (!showDialog) return

    val noButtons = confirmButtonText == null && dismissButtonText == null

    Dialog(onDismissRequest = { onDismiss?.invoke() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = containerColor,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 300.dp)
                .wrapContentHeight()
//                .wrapContentSize()
                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
//                horizontalAlignment = if (noButtons) Alignment.CenterHorizontally else Alignment.Start
            ) {
                // Title with icon always visible
                title?.let {
                    Row(
                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier,
                        horizontalArrangement = Arrangement.Center,
//                        horizontalArrangement = if (noButtons) Arrangement.Center else Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.error),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = titleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Main message
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        textAlign =  TextAlign.Center ,
//                        textAlign = if (noButtons) TextAlign.Center else TextAlign.Start,
                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Error details
                if (errorCode != null || errorMessage != null) {
                    val codeText = errorCode?.toString().orEmpty()
                    val msgText = errorMessage.orEmpty()
                    Text(
                        text = "Error $codeText: $msgText",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor,
                        textAlign =  TextAlign.Center,
//                        textAlign = if (noButtons) TextAlign.Center else TextAlign.Start,
                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Buttons row
                if (!noButtons) {
                    val dismissInteraction = remember { MutableInteractionSource() }
                    val isDismissFocused by dismissInteraction.collectIsFocusedAsState()
                    val confirmInteraction = remember { MutableInteractionSource() }
                    val isConfirmFocused by confirmInteraction.collectIsFocusedAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButtonText?.let { text ->
                            onDismiss?.let { action ->
                                TextButton(onClick = action) {
                                    Text(text, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                        confirmButtonText?.let { text ->
                            onConfirm?.let { action ->
                                TextButton(onClick = action) {
                                    Text(text, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
