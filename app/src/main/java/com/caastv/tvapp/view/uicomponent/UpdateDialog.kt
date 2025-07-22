package com.caastv.tvapp.view.uicomponent


import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun UpdateDialog(
    version: String,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNo,
        title   = { Text("Update available") },
        text    = { Text("v$version is ready—update now?") },
        confirmButton = {
            Button(onClick = onYes) { Text("Yes") }
        },
        dismissButton = {
            TextButton(onClick = onNo) { Text("No") }
        }
    )
}
