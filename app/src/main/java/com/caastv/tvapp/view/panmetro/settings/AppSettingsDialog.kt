package com.caastv.tvapp.view.panmetro.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.caastv.tvapp.model.data.settings.AppSettings
import com.caastv.tvapp.utils.uistate.PreferenceManager

@Composable
fun AppSettingsDialog(
    onToggle: (AppSettings) -> Unit,
    onBack: () -> Unit
) {
    var enablePlayerOverlay by remember { mutableStateOf(false) }
    var appSettings by remember { mutableStateOf<AppSettings?>(null) }

    // Focus management
    val switchFocus = remember { FocusRequester() }
    val backButtonFocus = remember { FocusRequester() }
    var currentFocus by remember { mutableStateOf("switch") }

    LaunchedEffect(Unit) {
        switchFocus.requestFocus()
        appSettings = PreferenceManager.getAppSettings()?: AppSettings()
        enablePlayerOverlay = appSettings?.isPlayerAnimationOverlay ?: false
    }


    // Handle D-pad navigation
    LaunchedEffect(currentFocus) {
        when (currentFocus) {
            "switch" -> switchFocus.requestFocus()
            "back" -> backButtonFocus.requestFocus()
        }
    }

    Dialog(onDismissRequest = onBack) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .3f))
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(min = 700.dp)
                    .padding(horizontal = 24.dp)
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "CAASTV",
                        color = Color(0xFF00E5FF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Preferences",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                ) {

                    // Player Animation Overlay Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3A3A3A),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Player Animation Overlay",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = enablePlayerOverlay,
                                onCheckedChange = {
                                    enablePlayerOverlay = it
                                    appSettings?.isPlayerAnimationOverlay = it
                                    appSettings?.let { settings ->
                                        onToggle(settings)
                                    }
                                },
                                modifier = Modifier
                                    .focusRequester(switchFocus)
                                    .height(32.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            currentFocus = "switch"
                                        }
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.height(80.dp))

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .focusRequester(backButtonFocus)
                            .width(220.dp)
                            .height(48.dp)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    currentFocus = "back"
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF00E5FF)
                        )
                    ) {
                        Text("Back", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
