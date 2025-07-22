package com.caastv.tvapp.view.otp

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.android.caastv.R
import com.caastv.tvapp.components.GradientBackground
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.caastv.tvapp.utils.theme.base_color

@Composable
fun OtpScreen1(navController: NavController) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    var timer by remember { mutableStateOf(30) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()


    // Create separate focus requesters for each box
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val firstBoxFocused = remember { mutableStateOf(false) } // Track if first input is focused

    // Start countdown timer
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000L)
            timer -= 1
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientBackground() // Background applied

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title Section
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(start = 128.dp, top = 32.dp, end = 128.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter OTP code",
                        style = TextStyle(
                            fontSize = 40.sp,
                            lineHeight = 52.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_medium)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Please enter the 6-digit code we’ve sent to +91 7010468377",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_medium)),
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                }
            }

            // OTP Input Fields
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                otpValues.forEachIndexed { index, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length == 1) {
                                otpValues = otpValues.toMutableList().also {
                                    it[index] = newValue
                                }

                                // Move focus to the next box after input
                                if (index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else {
                                    // Onexce all 6 digits are entered, stay on the last box and navigate
                                    keyboardController?.hide()
                                    if (otpValues.joinToString("").length == 6) {
                                        navController.navigate("epg_screen"){
                                            popUpTo(0)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            } else if (newValue.isEmpty()) {
                                otpValues = otpValues.toMutableList().also {
                                    it[index] = ""
                                }
                                // Move focus back if user deletes a digit
                                if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_light)),
                            fontWeight = FontWeight(400),
                            color = Color.White
                        ),
                        modifier = Modifier
                            .size(53.dp)
                            .focusRequester(focusRequesters[index])
                            .border(
                                width = 2.dp,
                                color = if (otpValues[index].isNotEmpty()) base_color else Color.Gray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = false) {}, // Disable click to prevent long-press issue
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Gray,
                            errorTextColor = Color.Red,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            cursorColor = base_color,
                            focusedIndicatorColor = base_color,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            // Resend OTP Timer
            Text(
                text = "Re-send code in 0:${timer.toString().padStart(2, '0')}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Cyan
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Ensure the first box is focused and keyboard remains open
            LaunchedEffect(Unit) {
                delay(500) // Give Compose some time to settle UI
                focusRequesters[0].requestFocus()
                keyboardController?.show()
            }
        }
    }
}




