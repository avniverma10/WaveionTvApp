package com.example.tvapp.view.panmetro

import androidx.compose.foundation.Image
import com.example.tvapp.model.data.epgdata.EPGDataItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.tvapp.R
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.res.painterResource


@Composable
fun TopOverlayInfo(dataItem: EPGDataItem?) {
    val now = System.currentTimeMillis()

    val programList = dataItem?.currentPrograms ?: dataItem?.tv?.programme

    val currentProgram = programList
        ?.sortedBy { it.startTime }
        ?.firstOrNull { (it.startTime ?: 0) <= now && (it.endTime ?: 0) > now }

    val startTimeFormatted = currentProgram?.startTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: "--"

    val endTimeFormatted = currentProgram?.endTime?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: "--"

    val minutesLeft = currentProgram?.endTime?.let {
        val diff = it - now
        (diff / 60000).toInt()
    } ?: 0

    // Live current time (if needed)
    val currentTime = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(System.currentTimeMillis())
            kotlinx.coroutines.delay(60_000) // update every minute
        }
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
                model = dataItem?.content?.thumbnailUrl,
                contentDescription = "Channel Logo",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dataItem?.content?.title ?: "No Information Available",
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
                    text = "$startTimeFormatted - $endTimeFormatted • $minutesLeft MIN LEFT",
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








