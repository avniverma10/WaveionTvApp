//package com.caastv.tvapp.view.player
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.border
//import androidx.compose.foundation.focusable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.interaction.collectIsFocusedAsState
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.ui.layout.FirstBaseline
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.Popup
//import androidx.compose.ui.window.PopupProperties
//import com.android.caastv.R
//import com.caastv.tvapp.utils.theme.base_color
//
//@Composable
//fun CommonDialog(
//    showDialog: Boolean,
//    title: String? = null,
//    message: String? = null,
//    errorCode: Int? = null,
//    errorMessage: String? = null,
//    borderColor: Color = Color.Gray,
//    confirmButtonText: String? = null,
//    onConfirm: (() -> Unit)? = null,
//    dismissButtonText: String? = null,
//    onDismiss: (() -> Unit)? = null,
//) {
//    if (!showDialog) return
//
//    val noButtons = confirmButtonText == null && dismissButtonText == null
//
//    Popup(
//        alignment = Alignment.Center,
//        properties = PopupProperties(
//            focusable             = !noButtons,
//            dismissOnBackPress    = false,
//            dismissOnClickOutside = false
//        )
//    ) {
//        Surface(
//            shape = RoundedCornerShape(16.dp),
//            tonalElevation = 8.dp,
//            color =  Color(0xFF191B1F),
//            modifier = Modifier
//                .padding(24.dp)
//                .widthIn(min = 200.dp, max = 400.dp)
//                .wrapContentHeight()
//                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                title?.let {
//                    var titleFontSize  by remember { mutableStateOf(19.sp) }
//                    var titleLineCount by remember { mutableStateOf(1) }
//                    Row(
//                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier,
//                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
//                        verticalAlignment     = if (titleLineCount > 1) Alignment.Top else Alignment.CenterVertically
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.error),
//                            contentDescription = null,
//                            modifier = Modifier.size(24.dp)
//                                .alignBy(FirstBaseline),
//                            colorFilter = ColorFilter.tint(Color.White)
//                        )
//                        Spacer(modifier = Modifier.width(2.dp))
//                        Text(
//                            text         = it,
//                            fontSize     = titleFontSize,
//                            color        = Color.White,
//                            textAlign    = TextAlign.Center,
//                            modifier     = Modifier.wrapContentWidth().alignBy(FirstBaseline),
////                            maxLines = 2,
////                            onTextLayout = { layout ->
////                                if (layout.lineCount > 1 && titleFontSize != 16.sp) {
////                                    titleFontSize = 17.sp
////                                }
////                                titleLineCount = layout.lineCount
////                            }
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(22.dp))
//                }
//
//                // Main message
//                message?.let {
//                    val msgFontSize = if (errorCode == null && errorMessage == null) 19.sp else 16.sp
//                    Text(
//                        text = it,
//                        fontSize = msgFontSize,
//                        color = Color.White,
//                        textAlign =  TextAlign.Center ,
//                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier
//                    )
//                    Spacer(modifier = Modifier.height(10.dp))
//                }
//
//                // Error details
//                if (errorCode != null || errorMessage != null) {
//                    val codeText = errorCode?.toString().orEmpty()
//                    val msgText = errorMessage.orEmpty()
//                    Text(
//                        text = "Error $codeText: $msgText",
//                        fontSize = 14.sp,
//                        color = Color.White,
//                        textAlign =  TextAlign.Center,
//                        modifier = if (noButtons) Modifier.fillMaxWidth() else Modifier.fillMaxWidth()
//                    )
//                    Spacer(modifier = Modifier.height(20.dp))
//                }
//                if ((errorCode == null && errorMessage == null) && !noButtons) {
//                    Spacer(modifier = Modifier.height(30.dp))
//                }
//
//                // Buttons row
//                if (!noButtons) {
//                    val dismissRequester    = remember { FocusRequester() }
//                    val dismissInteraction  = remember { MutableInteractionSource() }
//                    val isDismissFocused    by dismissInteraction.collectIsFocusedAsState()
//                    val confirmInteraction  = remember { MutableInteractionSource() }
//                    val isConfirmFocused    by confirmInteraction.collectIsFocusedAsState()
//
//                    LaunchedEffect(showDialog) {
//                        if (showDialog) dismissRequester.requestFocus()
//                    }
//
//                    Row(
//                        Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment   = Alignment.CenterVertically
//                    ) {
//                        dismissButtonText?.let { text ->
//                            onDismiss?.let { action ->
//                                TextButton(
//                                    onClick            = action,
//                                    interactionSource  = dismissInteraction,
//                                    modifier           = Modifier
//                                        .focusRequester(dismissRequester)
//                                        .focusable(interactionSource = dismissInteraction),
//                                    shape = RoundedCornerShape(8.dp),
//                                    colors             = ButtonDefaults.textButtonColors(
//                                        containerColor = if (isDismissFocused) base_color else Color(0xFF414857),
//                                        contentColor   = Color.Black
//                                    )
//                                ) {
//                                    Text(text)
//                                }
//                                Spacer(Modifier.width(28.dp))
//                            }
//                        }
//
//                        confirmButtonText?.let { text ->
//                            onConfirm?.let { action ->
//                                TextButton(
//                                    onClick            = action,
//                                    interactionSource  = confirmInteraction,
//                                    modifier           = Modifier
//                                        .focusable(interactionSource = confirmInteraction),
//                                    shape = RoundedCornerShape(8.dp),
//                                    colors             = ButtonDefaults.textButtonColors(
//                                        containerColor = if (isConfirmFocused) base_color else Color(0xFF414857),
//                                        contentColor   = Color.Black
//                                    )
//                                ) {
//                                    Text(text)
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//        }
//    }
//
//}
