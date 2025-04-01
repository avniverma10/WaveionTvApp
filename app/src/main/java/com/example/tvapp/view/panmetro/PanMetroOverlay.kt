package com.example.tvapp.view.panmetro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.tv.material3.MaterialTheme
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.tvapp.R
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.navigationhelper.Destination
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun PlayerOverlay(
    navController: NavController,
    dataItem: EPGDataItem?=null,
    onDismiss: () -> Unit
) {
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val formattedTime = remember { mutableStateOf("") }

    // Launch a coroutine that updates the time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime.value
            }
            val sdf = SimpleDateFormat("hh:mma | dd MMM", Locale.US)
            formattedTime.value = sdf.format(calendar.time)
            // Wait for 60 seconds before updating again
            delay(60_000)
        }
    }

    BackHandler {
        navController.navigate(Destination.genreScreen) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // A Box that covers the screen with a semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Catch clicks or key events so they don't pass through
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // A Column that holds your overlay UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
        ) {
            // Top Row: Channel Info, Time, etc.

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp)
            ) {
                // Channel info (logo, name, etc.)
                ChannelInfoSection(
                    dataItem= dataItem
                )

                Spacer(modifier = Modifier.weight(1f))

                // Clock or additional top-right info
                Row (
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(10.dp))
                        .padding(5.dp, 5.dp, 35.dp, 5.dp)
                        .align(Alignment.Bottom)
                ) {
                    Text(
                        text = formattedTime.value,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Middle Row: Program Info
            ProgramsInfoSection(dataItem= dataItem)

            /*Text(
                text = "\"Use arrow when this screen appears\"",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )*/

            IntentButtonSection()

        }

        // Example for dismissing overlay with Back or some key
        // (Alternatively, handle it in the parent or within onKeyEvent blocks.)
    }
}

@Composable
fun ChannelInfoSection(
    dataItem: EPGDataItem?=null
) {

    // For D-pad navigation, make it focusable if user can select it
    Box(
        modifier = Modifier
            .background(Color.Cyan.copy(.8f), shape = RoundedCornerShape(10.dp))
            .padding(30.dp, 5.dp, 20.dp, 5.dp)
            .onKeyEvent { keyEvent ->
                // Handle selection or directional keys if needed
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    // Possibly open channel menu or something
                    true
                } else false
            }
            .border(
                width =  0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        // Display channel name, logo, etc.
        Row(modifier = Modifier
            .align(Alignment.BottomCenter)) {
            Text(
                text = dataItem?.channelId?.replace("_"," ")?:"",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .border(2.dp, Color.Gray)
                    .background(Color.Gray)
                    .padding(5.dp)
            )
            Text(
                text = dataItem?.content?.title?:"",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(5.dp)
            )
        }
    }
}

@Composable
fun ProgramsInfoSection(
    dataItem: EPGDataItem?=null
) {
    val now = System.currentTimeMillis()
    // Suppose you have a field: val selectedChannelId = ...
    val filtered = dataItem?.currentPrograms?.sortedBy { it.startTime }

    val current = filtered?.firstOrNull { (it.startTime ?: 0) <= now && (it.endTime ?: 0) > now }
    val upcoming = filtered?.filter { (it.startTime ?: 0) > now }

    // For D-pad navigation, make it focusable if user can select it
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.6f))
           // .padding(5.dp, 5.dp, 20.dp, 5.dp)
            .onKeyEvent { keyEvent ->
                // Handle selection or directional keys if needed
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                    // Possibly open channel menu or something
                    true
                } else false
            }
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(start = 30.dp)
    ) {
        // Display channel name, logo, etc.
        Row(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)) {

            AsyncImage(
                model = dataItem?.content?.thumbnailUrl?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Black.copy(.5f), RoundedCornerShape(4.dp))
                    .padding(4.dp), // example icon padding
                contentScale = ContentScale.Fit
            )

            if(current != null && upcoming?.size?:0 >0) {

                Column {
                    Row(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color.Yellow, shape = RoundedCornerShape(5.dp))
                                .padding(5.dp, 5.dp, 5.dp, 5.dp)
                                .focusable(false) // Not focusable if it's just display
                        ) {
                            Text(
                                text = "Current Program",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "${current?.startTime ?: "00:00-00:00"}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(start = 10.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                    Text(
                        text = current?.title ?: "No information available",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(top = 15.dp)
                            .align(Alignment.CenterHorizontally)

                    )
                }
                Column(modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color.Green, shape = RoundedCornerShape(5.dp))
                            .padding(5.dp, 5.dp, 5.dp, 5.dp)
                            .focusable(false) // Not focusable if it's just display
                    ) {
                        Text(
                            text = "Next Program",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "${upcoming?.getOrNull(0)?.startTime ?: "00:00-00:00"}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Transparent)
                        )
                        Text(
                            text = upcoming?.getOrNull(0)?.title ?: " | No information available",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Transparent)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "${upcoming?.getOrNull(1)?.startTime ?: "00:00-00:00"}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Transparent)
                        )
                        Text(
                            text = upcoming?.getOrNull(1)?.title ?: " | No information available",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .background(Color.Transparent)
                        )
                    }
                }
            }else{
                Text(
                    text = "No information available",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 30.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Text(
                text = "Press OK to display this box",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()

            )
        }
    }
}


@Composable
fun IntentButtonSection(
) {
    val context = LocalContext.current
    var navigationFocus = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    // Container for the button row; this container is focusable for D-pad navigation.
    Box(
        modifier = Modifier
            .padding(30.dp, 5.dp, 20.dp, 5.dp)
            .focusRequester(navigationFocus)
            .focusable(interactionSource = interactionSource)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color.Yellow else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        // Row holding the three buttons.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Back) {
                        // For example, perform genre action
                        println("Genre button: Back key pressed")
                        true
                    } else false
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Genre Icon",
                    tint = Color.White
                )
                Text(text = "Genre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White)
            }

            Spacer(modifier = Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Genre Icon",
                    tint = Color.White
                )
                Text(text = "Catch Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White)
            }

            Spacer(modifier = Modifier.width(4.dp))
            Row(
                modifier = Modifier.onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.NavigateNext) {
                        // For example, perform genre action
                        println("Genre button: Back key pressed")
                        true
                    } else false
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Genre Icon",
                    tint = Color.White
                )
                Text(text = "EPG",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White)
            }
        }
    }
}
