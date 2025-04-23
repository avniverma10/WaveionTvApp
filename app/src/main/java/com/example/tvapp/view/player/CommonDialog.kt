package com.example.tvapp.view.player

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tvapp.R

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
                .wrapContentWidth()
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                title?.let {
                    Row(
                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier,
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.error),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = it,
                            fontSize = 19.sp,
                            color = titleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(17.dp))
                }

                // Main message
                message?.let {
                    val msgFontSize = if (errorCode == null && errorMessage == null) 19.sp else 14.sp
                    Text(
                        text = it,
                        fontSize = msgFontSize,
                        color = contentColor,
                        textAlign =  TextAlign.Center ,
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
                        fontSize = 13.sp,
                        color = contentColor,
                        textAlign =  TextAlign.Center,
                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier
                    )
                    Spacer(modifier = Modifier.height(38.dp))
                }
                if ((errorCode == null && errorMessage == null) && !noButtons) {
                    Spacer(modifier = Modifier.height(30.dp))
                }

                // Buttons row
                if (!noButtons) {
                    val dismissRequester    = remember { FocusRequester() }
                    val dismissInteraction  = remember { MutableInteractionSource() }
                    val isDismissFocused    by dismissInteraction.collectIsFocusedAsState()
                    val confirmInteraction  = remember { MutableInteractionSource() }
                    val isConfirmFocused    by confirmInteraction.collectIsFocusedAsState()

                    LaunchedEffect(showDialog) {
                        if (showDialog) dismissRequester.requestFocus()
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        dismissButtonText?.let { text ->
                            onDismiss?.let { action ->
                                TextButton(
                                    onClick            = action,
                                    interactionSource  = dismissInteraction,
                                    modifier           = Modifier
                                        .focusRequester(dismissRequester)
                                        .focusable(interactionSource = dismissInteraction),
                                    shape = RoundedCornerShape(8.dp),
                                    colors             = ButtonDefaults.textButtonColors(
                                        containerColor = if (isDismissFocused) Color(0xFF49FEDD) else Color(0xFF414857),
                                        contentColor   = Color.Black
                                    )
                                ) {
                                    Text(text)
                                }
                                Spacer(Modifier.width(28.dp))
                            }
                        }

                        confirmButtonText?.let { text ->
                            onConfirm?.let { action ->
                                TextButton(
                                    onClick            = action,
                                    interactionSource  = confirmInteraction,
                                    modifier           = Modifier
                                        .focusable(interactionSource = confirmInteraction),
                                    shape = RoundedCornerShape(8.dp),
                                    colors             = ButtonDefaults.textButtonColors(
                                        containerColor = if (isConfirmFocused) Color(0xFF49FEDD) else Color(0xFF414857),
                                        contentColor   = Color.Black
                                    )
                                ) {
                                    Text(text)
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
