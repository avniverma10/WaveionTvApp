package com.example.tvapp.view.panmetro.common

import android.annotation.SuppressLint
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.android.panmetroiptv.R
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.player.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun TopOverlayInfo(sharedViewModel: SharedViewModel,playerViewModel: PlayerViewModel) {
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val programList by playerViewModel.filterAvailablePrograms.collectAsState()
    val timeLeft by playerViewModel.currentProgramMinutesLeft.collectAsState()
    var currentProgram = programList.getOrNull(0)

    LaunchedEffect(programList) {
        currentProgram = programList.getOrNull(0)
        //currentProgram?.let { playerViewModel.updateCurrentRunningProgramTimings(it) }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Logo
        AsyncImage(
            model = selectedChannel.content?.thumbnailUrl,
            contentDescription = "Channel Logo",
            modifier = Modifier.size(70.dp)
        )

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
                text = "${currentProgram?.startFormatedTime} - ${currentProgram?.endFormatedTime} • ${playerViewModel.provideCurrentRunningProgramTimings(currentProgram)} MIN LEFT",
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








