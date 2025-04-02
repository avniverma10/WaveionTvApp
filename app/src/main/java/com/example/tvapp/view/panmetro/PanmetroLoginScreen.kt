package com.example.tvapp.view.panmetro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvapp.R
import com.example.tvapp.view.uicomponent.GradientBackground
import com.example.tvapp.viewmodels.SharedViewModel

@Composable
fun PanmetroLoginScreen(
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current

    // Outer Box with dark blue background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar stays at the top
            PermettoTopBar()

            // Gradient background placed directly under the top header
            GradientBackground {
                // The login UI is placed within the gradient background
                LoginScreenUI()
            }
        }
    }
}

@Composable
fun LoginScreenUI() {
    val figtreeMedium = FontFamily(Font(R.font.figtree_medium, FontWeight.Bold))
    val figtreeLight = FontFamily(Font(R.font.figtree_light, FontWeight.Bold))
    // Center the login UI elements in a Row
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT SIDE: Pink gradient box with rounded left corners
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7F00FF),
                            Color(0xFFE100FF)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Circular user icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile1),
                        contentDescription = "Arrow Icon",
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Image(
                    painter = painterResource(id = R.drawable.reddot),
                    contentDescription = "Circular Image",
                    modifier = Modifier
                        .size(25.dp)               // Control how large the image should be
                        .clip(CircleShape)         // Clip to a circle shape
                        .background(Color.LightGray)  // Optional background behind the image
                )
                Spacer(modifier = Modifier.height(15.dp))

                // Static Username field
                LoginItem(iconResId = R.drawable.user_p, label = "myUser")
                Spacer(modifier = Modifier.height(8.dp))

                // Static Password field
                LoginItem(iconResId = R.drawable.lock, label = "********")
                Spacer(modifier = Modifier.height(8.dp))

                // Static MAC ID field
                LoginItem(iconResId = R.drawable.user1, label = "12:34:56:78:9A:BC")
                Spacer(modifier = Modifier.height(8.dp))

                LoginItem(iconResId = R.drawable.mac, label = "Mac id")
                Spacer(modifier = Modifier.height(20.dp))

                // Login button row (static)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable {
                            // Optionally handle click for navigation
                        }
                        .padding(8.dp)
                ) {
                    Text(text = "Login", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Up Icon",
                        tint = Color.Black
                    )
                    // Uncomment if you want an arrow icon:
                    // Image(
                    //     painter = painterResource(id = R.drawable.ic_arrow_forward),
                    //     contentDescription = "Arrow Icon",
                    //     modifier = Modifier.size(24.dp)
                    // )
                }
            }
        }
        // RIGHT SIDE: Teal box with rounded right corners
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(400.dp)
                .background(
                    Color(0xFF00BFFF),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Panmetro logo
                Image(
                    painter = painterResource(id = R.drawable.panlogin),
                    contentDescription = "Panmetro Logo",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Main title
                Text(
                    text = "Panmetro-IPTV",
                    color = Color.Black,
                    fontSize = MaterialTheme.typography.h5.fontSize,
                    fontFamily = figtreeMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Subtitle/tagline
                Text(
                    text = "Future of entertainment",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = figtreeLight
                )
            }
        }
    }
}

/**
 * A helper composable to display a row with an icon and static text.
 */
@Composable
fun LoginItem(iconResId: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.Black)

    }
}

