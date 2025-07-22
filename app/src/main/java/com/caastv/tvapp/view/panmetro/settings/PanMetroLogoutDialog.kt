package com.caastv.tvapp.view.panmetro.settings


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
fun PanMetroLogoutDialog(
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
                    text = "Do you want to Logout?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
            }
        },
        text = {
            Text(
                text = "Press Yes to logout or No to stay.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        },
        confirmButton = {
            Button(onClick = {
                isSelected = true
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
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    Color.Gray,
                    contentColor = Color.White
                )) {
                Text("No")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
