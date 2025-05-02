package com.example.tvapp.view.panmetro.login

import android.app.Activity
import android.os.Process
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.components.GradientBackground
import com.example.tvapp.extensions.getAndroidTvDrmInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.CommonDialog
import com.example.tvapp.viewmodels.LoginViewModel

@Composable
fun PanmetroLoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val context    = LocalContext.current
    val macAddress = context.provideMacAddress()
    val loginInfo  = loginViewModel.loginInfo?.collectAsState()?.value

    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var username   by remember { mutableStateOf("avni") }
    var password   by remember { mutableStateOf("123") }
    var macId      by remember { mutableStateOf(macAddress) }
    var rememberMe by remember { mutableStateOf(false) }

    val passwordFocusRequester = remember { FocusRequester() }
    val loginFocusRequester    = remember { FocusRequester() }

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonFocused by buttonInteractionSource.collectIsFocusedAsState()

    val figtreeMedium = FontFamily(Font(R.font.figtree_medium, FontWeight.Bold))

    LaunchedEffect(Unit) {
        loginInfo?.let {
            if (it.rememberMe) {
                username   = it.username
                password   = it.password
                rememberMe = true
            }
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            message = "Are you sure you want to exit the app?",
            errorCode = null,
            errorMessage = null,
            borderColor = Color.Transparent,
            confirmButtonText = "Yes",
            onConfirm = {
                (context as? Activity)?.finishAffinity()
                Process.killProcess(Process.myPid())
            },
            dismissButtonText = "No",
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "CAASTV",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3) Centered Content (Welcome + Form)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Text
            Text(
                text = "Welcome to CAASTV",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = figtreeMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login Form
            Column(
                modifier = Modifier
                    .width(350.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(base_color),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.user_icon),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        usernameError = it.isBlank()
                        username = it
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    isError = usernameError,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = base_color,
                        unfocusedBorderColor = Color.White,
                        cursorColor = base_color,
                        focusedLabelColor = base_color,
                        unfocusedLabelColor = Color.White,
                        textColor = Color.White
                    )
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        passwordError = it.isBlank()
                        password = it
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { loginFocusRequester.requestFocus() }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = base_color,
                        unfocusedBorderColor = Color.White,
                        cursorColor = base_color,
                        focusedLabelColor = base_color,
                        unfocusedLabelColor = Color.White,
                        textColor = Color.White
                    )
                )

                // MAC ID (Read-only)
                OutlinedTextField(
                    value = macId ?: "",
                    onValueChange = {},
                    label = { Text("MAC ID") },
                    singleLine = true,
                    leadingIcon = {
                        Image(
                            painter      = painterResource(R.drawable.mac_id_icon),
                            contentDescription = null,
                            modifier     = Modifier
                                .size(30.dp)
                                .padding(4.dp),
                            colorFilter  = ColorFilter.tint(Color.White)  // force white tint
                        )
                    },
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor   = base_color,
                        unfocusedBorderColor = Color.White,
                        cursorColor          = base_color,
                        focusedLabelColor    = base_color,
                        unfocusedLabelColor  = Color.White,
                        textColor            = Color.White
                    )
                )

                Spacer(Modifier.height(20.dp))
                // Login Button
                Button(
                    onClick = {
                        var valid = true
                        var msg = ""
                        if (username.isBlank()) { valid = false; msg = "Username should not be blank" }
                        else if (password.isBlank()) { valid = false; msg = "Password should not be blank" }
                        if (valid) {
                            (context as? Activity)?.hideKeyboard()
                            context.getAndroidTvDrmInfo()
                                ?.copy(userName = username, userPassword = password)
                                ?.let { info ->
                                    loginViewModel.validateUserLogin(
                                        androidTvDrmInfo = info,
                                        onLoginResponse = { response, errorMsg ->
                                            if (response != null) {
                                                loginViewModel.saveLogin(username, password, rememberMe)
                                                navController.navigate(Destination.genreScreen)
                                            } else {
                                                context.showToastS(errorMsg ?: "Login failed")
                                            }
                                        }
                                    )
                                }
                        } else context.showToastS(msg)
                    },
                    interactionSource = buttonInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            BorderStroke(if (isButtonFocused) 2.dp else 0.dp, base_color),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .focusRequester(loginFocusRequester),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonFocused) base_color else Color.White,
                        contentColor = if (isButtonFocused) Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = "Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = figtreeMedium
                    )
                }
            }
        }
    }
}
