package com.android.panmetroiptv.view.network

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    onExitAppClick: () -> Unit,
    initialFocusOnConfirm: Boolean = true
) {

    val exitAppRequester   = remember { FocusRequester() }
    val exitAppInteraction = remember { MutableInteractionSource() }
    val isExitAppFocused   by exitAppInteraction.collectIsFocusedAsState()

    val networkSettingsRequester   = remember { FocusRequester() }
    val networkSettingsInteraction = remember { MutableInteractionSource() }
    val isNetworkFocused   by networkSettingsInteraction.collectIsFocusedAsState()

    LaunchedEffect(Unit) {
        if (initialFocusOnConfirm) {
            networkSettingsRequester.requestFocus()
        }else {
            exitAppRequester.requestFocus()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                // This makes all children inside this Column be treated as one navigable group.
                .focusGroup()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TCCL",
                color = Color.White,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "The network is unstable.\nPlease check the connection status and try again.",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onNetworkSettingsClick,
                interactionSource = networkSettingsInteraction,
                modifier = Modifier
                    .focusRequester(networkSettingsRequester)
                    .focusable(interactionSource = networkSettingsInteraction),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if (isNetworkFocused) Color(0x1A49FEDD) else Color(0xFF414857),
                    contentColor = if (isNetworkFocused) Color.White else Color.Black
                ),
                contentPadding = PaddingValues(
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

            Button(
                onClick = onExitAppClick,
                interactionSource = exitAppInteraction,
                modifier = Modifier
                    .focusRequester(exitAppRequester)
                    .focusable(interactionSource = exitAppInteraction),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = if (isExitAppFocused) Color(0x1A49FEDD) else Color(0xFF414857),
                    contentColor = if (isExitAppFocused) Color.White else Color.Black
                ),
                contentPadding = PaddingValues(
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
