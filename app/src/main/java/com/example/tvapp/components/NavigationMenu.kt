package com.example.tvapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.example.tvapp.R
import com.example.tvapp.screens.MenuItem
import com.example.tvapp.viewmodels.EPGViewModel

@Composable
fun NavigationMenu(viewModel: EPGViewModel = hiltViewModel()) {
    val menuItems = listOf(
        MenuItem(R.drawable.all, "ALL"),
        MenuItem(R.drawable.recent, "Recently Watched"),
        MenuItem(R.drawable.news, "News"),
        MenuItem(R.drawable.face, "Entertainment"),
        MenuItem(R.drawable.music, "Music"),
        MenuItem(R.drawable.kid, "Kids"),
        MenuItem(R.drawable.spirit, "Spiritual"),
        MenuItem(R.drawable.movie, "Movies"),
        MenuItem(R.drawable.star, "Lifestyle")
    )

    Column(
        modifier = Modifier
            .width(200.dp) // Fixed width
            .fillMaxHeight()
            .background(Color.DarkGray)
            .padding(top = 16.dp)
    ) {
        menuItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .clickable {
                            viewModel.filterChannelsByGenre(item.label) // ✅ Filter Channels
                    },
                verticalAlignment = Alignment.CenterVertically // Align icon and text
            ) {
                Icon(
                    painter = painterResource(id = item.icon), // Load icon from resources
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp) // Adjust icon size
                )

                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}