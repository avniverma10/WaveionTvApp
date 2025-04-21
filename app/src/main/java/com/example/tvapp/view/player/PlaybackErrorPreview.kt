package com.example.tvapp.view.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvapp.R  // adjust your package

@Composable
fun PlaybackErrorCard(
    errorCode: Int,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (errorCode in 606..700) Color(0xFF6B2828) else Color.Green

    Surface(
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 30.dp,
        color = Color(0xFF191B1F),
        modifier = modifier
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .width(200.dp)
            .height(83.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Row with error icon + title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.error),
                    contentDescription = "Error icon",
                    modifier = Modifier.size(17.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Playback Error",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "The video cannot be played",
                fontSize = 11.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(1.dp))

            Text(
                text = "Error $errorCode : $errorMessage",
                fontSize = 10.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
