package com.caastv.tvapp.view.uicomponent.fingerprint.globalfingerprint

// File: OverlayPermissionDialog.kt

import androidx.compose.runtime.*
import com.caastv.tvapp.view.uicomponent.error.CommonDialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.android.caastv.R


@Composable
fun OverlayPermissionDialog(onRequest: () -> Unit) {
    var show by remember { mutableStateOf(true) }
    if (show) {
        CommonDialog(
            showDialog = true,
            painter = painterResource(id = R.drawable.permission_icon),
            title = "Permission Required",
            borderColor = Color.Transparent,
            message = "The app needs “Draw Over Other Apps” permission. Please grant it.",
            confirmButtonText = "Open Settings",
            onConfirm = {
                show = false
                onRequest()
            },
            dismissButtonText = null,
            onDismiss = null,
            errorCode = null,
            errorMessage = null,
            initialFocusOnConfirm = true
        )
    }
}
