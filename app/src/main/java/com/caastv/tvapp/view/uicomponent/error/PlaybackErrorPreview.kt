package com.caastv.tvapp.view.uicomponent.error

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.android.caastv.R

@Composable
fun PlaybackErrorPreview(
    errorCode: Int,
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (errorCode in 606..700) Color(0xFF6B2828) else Color(0xFF49FEDD)
    val firstSentence = errorMessage
        .let { msg ->
            val idx = msg.indexOf('.')
            if (idx >= 0) msg.substring(0, idx + 1) else msg
        }

    Surface(
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 30.dp,
        color = Color(0xFF191B1F),
        modifier = modifier
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .width(200.dp)
            .height(85.dp)
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
                    painter = painterResource(id = R.drawable.media_error),
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
                text = "Error $errorCode : $firstSentence",
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
