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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import com.android.caastv.R
import com.example.tvapp.components.GradientBackground
import com.example.tvapp.extensions.getAndroidTvDrmInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.viewmodels.LoginViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PanmetroLoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val context    = LocalContext.current
    val macAddress = context.provideMacAddress()

    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var username   by remember { mutableStateOf("avni") }
    var password   by remember { mutableStateOf("123") }
    var macId      by remember { mutableStateOf(macAddress) }

    val passwordFocusRequester = remember { FocusRequester() }
    val loginFocusRequester    = remember { FocusRequester() }

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonFocused by buttonInteractionSource.collectIsFocusedAsState()

    val figtreeMedium = FontFamily(Font(R.font.figtree_medium, FontWeight.Bold))

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            message = "Are you sure you want to exit the app?",
            painter = painterResource(id = R.drawable.exit_icon),
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()        // respect IME
            .imeNestedScroll()   // allow nested scrolling
    ) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to CAASTV",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = figtreeMedium
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .width(350.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        usernameError = it.isBlank()
                        username = it
                    },
                    label = { Text("Username",color = Color.White ) },
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

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        passwordError = it.isBlank()
                        password = it
                    },
                    label = { Text("Password", color = Color.White  ) },
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

                // MAC ID read-only
                OutlinedTextField(
                    value = macId.orEmpty(),
                    onValueChange = {},
                    label = { Text("MAC ID",color = Color.White) },
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

                // **Login Button** with adaptive text color
                Button(
                    onClick = {
                        var valid = true
                        var msg = ""
                        if (username.isBlank()) {
                            valid = false; msg = "Username should not be blank"
                        } else if (password.isBlank()) {
                            valid = false; msg = "Password should not be blank"
                        }
                        if (valid) {
                            (context as? Activity)?.hideKeyboard()
                            context.getAndroidTvDrmInfo()
                                ?.copy(userName = username, userPassword = password)
                                ?.let { info ->
                                    loginViewModel.validateUserLogin(
                                        androidTvDrmInfo = info,
                                        onLoginResponse = { response, errorMsg ->
                                            if (response != null) {
                                                PreferenceManager.saveLogin(username, password)
                                                context.hideKeyboard()
                                                navController.navigate(Destination.genreScreen)
                                            } else {
                                                context.showToastS(errorMsg ?: "Login failed")
                                            }
                                        }
                                    )
                                }
                        } else {
                            context.showToastS(msg)
                        }
                    },
                    interactionSource = buttonInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            BorderStroke(
                                width = if (isButtonFocused) 2.dp else 0.dp,
                                color = Color(0xFF49FEDD)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .focusRequester(loginFocusRequester),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonFocused) Color(0x1A49FEDD) else Color.White,
                        contentColor   = if (isButtonFocused) Color.White       else Color.Black
                    )
                ) {
                    Text(
                        text       = "Login",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = figtreeMedium
                        // no `color = …` here—Material-3 Text will use the Button’s contentColor
                    )
                }
            }
        }
    }
}
