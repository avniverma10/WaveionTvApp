package com.panmetro.iptv.view.panmetro.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.viewmodels.player.PlayerViewModel


@Composable
fun TopOverlayInfo(playerViewModel: PlayerViewModel) {
    val currentChannel by playerViewModel.selectedEPG.collectAsState()
    val currentProgram by playerViewModel.selectedProgram.collectAsState()
    val timeLeft by playerViewModel.selectedTimeLeft.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Logo
        AsyncImage(
            model = currentChannel?.thumbnailUrl,
            contentDescription = "Channel Logo",
            modifier = Modifier.size(70.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentChannel?.title ?: "No Information Available",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.01.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_black)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFB5B5B5)
                )
            )
            Spacer(modifier = Modifier.width(20.dp))
            loge("AVNI","${currentProgram?.startFormatedTime} - ${currentProgram?.endFormatedTime} ")
            Text(
                text = "${currentProgram?.startFormatedTime} - ${currentProgram?.endFormatedTime} • ${timeLeft} MIN LEFT",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.vector_271),
                contentDescription = "Progress Indicator",
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Live",
                color = Color.LightGray,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFB5B5B5)
                )
            )
        }
    }
}








