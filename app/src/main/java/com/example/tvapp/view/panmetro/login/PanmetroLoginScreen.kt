package com.example.tvapp.view.panmetro.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.android.panmetroiptv.R
import com.example.tvapp.extensions.getAndroidTvDrmInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.loge
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.extensions.toResponseMessage
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.panmetro.common.PermettoTopBar
import com.example.tvapp.view.uicomponent.ExitDialog
import com.example.tvapp.view.uicomponent.GradientBackground
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.LoginViewModel
import com.example.tvapp.viewmodels.SharedViewModel

@Composable
fun PanmetroLoginScreen(
    sharedViewModel: SharedViewModel,
    loginViewModel: LoginViewModel?= hiltViewModel(),navController: NavController
) {
    HideKeyboardOnEnter()
    val context = LocalContext.current
    val macAddress = context.provideMacAddress()
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val loginFocusRequester = remember { FocusRequester() }
    var username by remember { mutableStateOf("PAN00005") }
    var password by remember { mutableStateOf("123456") }
    var macId by remember { mutableStateOf(macAddress) }
    val figtreeMedium = FontFamily(Font(R.font.figtree_medium, FontWeight.Bold))
    val figtreeLight = FontFamily(Font(R.font.figtree_light, FontWeight.Bold))

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }



    // Exit confirmation dialog
    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            message = "Are you sure you want to exit the app?",
            borderColor = Color.Transparent,
            painter = painterResource(id = R.drawable.exit_icon),
            errorCode = null,
            errorMessage = null,
            confirmButtonText = "Yes",
            onConfirm = {
                (context as? Activity)?.finishAffinity()
            },
            dismissButtonText = "No",
            onDismiss = {
                showExitDialog = false
            }
        )
    }


    // Outer Box with dark blue background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar stays at the top
            PermettoTopBar()

            // Gradient background placed directly under the top header
            GradientBackground {
                // The login UI is placed within the gradient background
                // Center the login UI elements in a Row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SIDE: Pink gradient box with rounded left corners
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(400.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF7C07F1),
                                        Color(0xFFD30AEE)
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Circular user icon
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.profile1),
                                    contentDescription = "Arrow Icon",
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Image(
                                painter = painterResource(id = R.drawable.reddot),
                                contentDescription = "Circular Image",
                                modifier = Modifier
                                    .size(15.dp)               // Control how large the image should be
                                    .clip(CircleShape)         // Clip to a circle shape
                                    .background(Color.LightGray)  // Optional background behind the image
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    usernameError = it.isBlank()
                                },
                                label = { Text("UserName") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.user_p),
                                        contentDescription = "Username Icon",
                                        tint = Color.Unspecified
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent, shape = RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                                    .focusRequester(usernameFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        try {
                                            passwordFocusRequester.requestFocus()
                                        } catch (e: IllegalStateException) {
                                            loge("FocusError", "FocusRequester not initialized")
                                        }
                                    }
                                ),

                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Green,
                                    unfocusedBorderColor = Color.White,
                                    cursorColor = Color.Green,
                                    focusedLabelColor = Color.Green,
                                    unfocusedLabelColor = Color.White,
                                    textColor = Color.White, // hides from D-pad navigation and disables focus highlighting
                                ),
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = it.isBlank()
                                },
                                label = { Text("Password") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.lock),
                                        contentDescription = "Password Icon",
                                        tint = Color.Unspecified
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = passwordError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent, shape = RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                                    .focusRequester(passwordFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        try {
                                            loginFocusRequester.requestFocus()
                                        } catch (e: IllegalStateException) {
                                            loge("FocusError", "FocusRequester not initialized")
                                        }
                                    }
                                ),

                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Green,
                                    unfocusedBorderColor = Color.White,
                                    cursorColor = Color.Green,
                                    focusedLabelColor = Color.Green,
                                    unfocusedLabelColor = Color.White,
                                    textColor = Color.White, // hides from D-pad navigation and disables focus highlighting
                                ),
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            OutlinedTextField(
                                value = macId?:"",
                                onValueChange = {
                                    macId = it
                                },
                                label = { Text("MacId") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.user1),
                                        contentDescription = "MacId Icon",
                                        tint = Color.Unspecified
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent, shape = RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                                    .focusable(false), // optional if you also want to prevent input completely
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Green,
                                    unfocusedBorderColor = Color.White,
                                    cursorColor = Color.Green,
                                    focusedLabelColor = Color.Green,
                                    unfocusedLabelColor = Color.White,
                                    textColor = Color.White, // hides from D-pad navigation and disables focus highlighting
                                ),
                                enabled = false

                            )
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(top = 5.dp),
                                verticalAlignment = Alignment.CenterVertically) {

                                // Next Button with Arrow
                                Button(
                                    onClick = {
                                        var isValid = true
                                        var msg = ""
                                        if(username.isEmpty()){
                                            isValid = false
                                            msg = "username should not be blank"
                                        }else if(password.isEmpty()){
                                            isValid = false
                                            msg = "password should not be blank"
                                        }
                                        if(isValid) {
                                            context.hideKeyboard()
                                            context.getAndroidTvDrmInfo()?.copy(
                                                userName = username,
                                                userPassword = password,
                                                macId = macId?:""
                                            )?.let {deviceLoginInfo->
                                                loginViewModel?.validateUserLogin(androidTvDrmInfo = deviceLoginInfo,onLoginResponse={response,errorMsg->
                                                    // Optionally handle click for navigation
                                                    if (response?.returncode?.equals("0",true) == true){
                                                        // On Login Success:
                                                        PreferenceManager.saveLogin(username, password)
                                                        PreferenceManager.saveUserInfo(response)
                                                        context.hideKeyboard()
                                                        navController.navigate(Destination.genreScreen) {
                                                            popUpTo(Destination.loginScreen) { inclusive = true }
                                                        }
                                                    }else {
                                                        context.showToastS(response?.returncode?.toResponseMessage())

                                                    }
                                                })
                                            }
                                        }else{
                                            context.showToastS(msg)
                                        }
                                    },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .wrapContentWidth()
                                        .focusable()
                                        .padding(start =10.dp,end=10.dp)
                                        .focusRequester(loginFocusRequester),
                                    shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF7A17E8), // Green background
                                        contentColor = Color.White           // Text color
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {

                                        Text(
                                            text = "Next",
                                            color = Color.White,
                                            fontSize = 16.sp, // set your desired font size here
                                            fontWeight = FontWeight.Bold // make the text bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Next Arrow",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // RIGHT SIDE: Teal box with rounded right corners
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .height(400.dp)
                            .background(
                                Color(0xFF00BFFF),
                                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Panmetro logo
                            Image(
                                painter = painterResource(id = R.drawable.panlogin),
                                contentDescription = "Panmetro Logo",
                                modifier = Modifier.size(150.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Main title
                            Text(
                                text = "Panmetro-IPTV",
                                color = Color.Black,
                                fontSize = MaterialTheme.typography.h5.fontSize,
                                fontFamily = figtreeMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Subtitle/tagline
                            Text(
                                text = "Future of entertainment",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = figtreeLight
                            )
                        }
                    }
                }
            }
        }
    }
}


