package com.example.tvapp.view.network

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetworkUnstableScreen(
    onNetworkSettingsClick: () -> Unit,
    onExitAppClick: () -> Unit
) {
    // We'll use a FocusRequester to place initial focus on the "Network Settings" button
    val networkSettingsRequester = remember { FocusRequester() }

    // As soon as the composable is first composed, request focus on the first button
    LaunchedEffect(Unit) {
        networkSettingsRequester.requestFocus()
    }

    // Container that fills the entire screen, dark background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // TV UIs are often dark
        contentAlignment = Alignment.Center
    ) {
        // Arrange everything in a vertical column, centered horizontally
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp) // add horizontal padding so text doesn't go edge‐to‐edge
        ) {
            // 1) Large title: “Apps”
            Text(
                text = "TCCL",
                color = Color.White,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2) Two‐line message
            Text(
                text = "The network is unstable.\nPlease check the connection status and try again.",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3) “Network Settings” button (default focused)
            Button(
                onClick = onNetworkSettingsClick,
                modifier = Modifier
                    .focusRequester(networkSettingsRequester)
                    .focusable(), // allows D-pad focus
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1F1F1F),  // dark gray background
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
            ) {
                Text(
                    text = "Network Settings",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4) “Exit App” button
            Button(
                onClick = onExitAppClick,
                modifier = Modifier.focusable(), // can be focused after user navigates down
                colors = ButtonDefaults.buttonColors(
                    backgroundColor =Color(0xFF3A3A3A), // slightly lighter gray,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
            ) {
                Text(
                    text = "Exit App",
                    fontSize = 20.sp
                )
            }
        }
    }
}