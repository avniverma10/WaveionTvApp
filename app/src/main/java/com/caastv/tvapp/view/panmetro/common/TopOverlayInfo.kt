package com.caastv.tvapp.view.panmetro.common

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.caastv.tvapp.viewmodels.SharedViewModel
import com.caastv.tvapp.viewmodels.player.PlayerViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TopOverlayInfo(sharedViewModel: SharedViewModel,playerViewModel: PlayerViewModel) {
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val programList by sharedViewModel.filterAvailablePrograms.collectAsState()
    val timestamp by playerViewModel.timestampFlow()
        .collectAsState(initial = System.currentTimeMillis())


    val stops = selectedChannel.content
        ?.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.mapNotNull {
            runCatching {
                Color(android.graphics.Color.parseColor(it.color))
            }.getOrNull()
        }
        .orEmpty()

    val logoBrush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        SolidColor(Color(0xFF232020)) // fallback
    }
    var programIndex = remember { 0 }

    val currentProgram = remember(programIndex) {
        programList.getOrNull(programIndex)
    }

    // 2) Format it once per emission
    val timeLeft = remember(timestamp) {
        val diff = programList.getOrNull(programIndex)?.endTime?.minus(timestamp) ?: 0
        if( diff > 0){
           val timeLeft = diff.div(60000).toInt()
            if(timeLeft == 0){
                1
            }else{
                timeLeft
            }
        }else{
            programIndex +=1
            (programList.getOrNull(programIndex)?.endTime?.minus(timestamp)?.div(60000))?.toInt()?:0
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(logoBrush)
        ) {
            AsyncImage(
                model = selectedChannel.content?.thumbnailUrl,
                contentDescription = "Channel Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }


        Spacer(modifier = Modifier.width(16.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedChannel.content?.title ?: "No Information Available",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.01.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_black)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFB5B5B5)
                )
            )
            Spacer(modifier = Modifier.width(20.dp))
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








