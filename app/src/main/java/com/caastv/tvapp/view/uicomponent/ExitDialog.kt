package com.caastv.tvapp.view.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ExitDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    // Track the selected state of the confirm button
    var isSelected by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            // Title area with an icon + text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // For demonstration, using an Info icon.
                // Replace with a "sad face" or any other icon resource you prefer.
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sure you want to exit?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
            }
        },
        text = {
            // Optional subtext or explanation
            Text(
                text = "Press Yes to close the application or No to stay.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        },
        confirmButton = {
            // "Yes" Button -> exit the application
            Button(onClick = {
                isSelected = true // update state when clicked
                onConfirmExit()
            },
                colors = ButtonDefaults.buttonColors(
                    // Change the background color based on selection state
                    if (isSelected) Color.Green else Color.Gray,
                    contentColor = Color.White
                )) {
                Text("Yes")
            }
        },
        dismissButton = {
            // "No" Button -> dismiss dialog
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    Color.Gray,
                    contentColor = Color.White
                )) {
                Text("No")
            }
        },
        shape = RoundedCornerShape(16.dp),  // Rounded corners
        containerColor = Color.White        // Dialog background color
    )
}
