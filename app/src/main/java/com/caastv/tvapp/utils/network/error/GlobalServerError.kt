package com.caastv.tvapp.utils.network.error

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.caastv.tvapp.utils.network.ErrorHandler

@Composable
fun GlobalServerError() {
    val context = LocalContext.current
    val errorState = ErrorHandler.errorState.collectAsState()

    var currentOffset by remember { mutableStateOf(Offset.Zero) }


    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val density = LocalDensity.current

    errorState?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = with(density) { currentOffset.x.dp }, top = with(density) { currentOffset.y.dp })
        ) {
            /*Text(
                text = displayMessage,
                fontSize = fingerprintRule.value.fontSizeDp?.toString().getIntValue().sp,
                color = fontColor,
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )*/
        }
    }
}
