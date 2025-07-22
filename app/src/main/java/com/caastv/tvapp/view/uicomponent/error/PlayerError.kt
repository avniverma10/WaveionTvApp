package com.caastv.tvapp.view.uicomponent.error

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.android.caastv.R
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerError(
    modifier: Modifier,
    errorCode: Int,
    errorMessage: String
) {
    val borderColor = remember(errorCode) {
        if (errorCode in 606..700) Color(0xFF6B2828) else Color(0xFF49FEDD)
    }

    Box(modifier = modifier
        .shadow(
            elevation = 30.dp,
            shape = RoundedCornerShape(8.dp),
            clip = false
        )
        .border(
            BorderStroke(2.dp, borderColor),
            shape = RoundedCornerShape(8.dp)
        )
        .clip(RoundedCornerShape(8.dp))) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = "Error icon",
                        modifier = Modifier.size(19.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Playback Error",
                        fontSize = 18.sp,
                        color = Color(0xFFE0E0E0)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The video cannot be played",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = "Error $errorCode : $errorMessage",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

            }

    }
}
