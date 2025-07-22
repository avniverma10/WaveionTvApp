package com.caastv.tvapp.utils.network.error

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.caastv.tvapp.extensions.showToastServer
import com.caastv.tvapp.utils.network.ErrorHandler

@Composable
fun GlobalErrorHandler(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = { ErrorHandler.clearError() }
) {
    val context = LocalContext.current
    val errorState = ErrorHandler.errorState.collectAsState()

    var isAlreadyShowing by remember { mutableStateOf(false) }

   /* LaunchedEffect(errorState) {
        if(errorState.value?.isNetworkError == true && !isAlreadyShowing ){
            isAlreadyShowing = true
        }else if(errorState.value?.isNetworkError == true){
            isAlreadyShowing = true
        }
    }*/


    errorState.value?.let {
        context.showToastServer("${errorState.value?.code} | ${errorState.value?.title}")
        //context.showToastS("${errorState.value?.code}:${errorState.value?.title}")
        /*Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            val borderColor = remember(errorState.value?.code) {
                if (errorState.value?.code in 700..800) Color(0xFF49FEDD) else Color(0xFF6B2828)
            }

            CommonDialog(
                painter = painterResource(id = R.drawable.media_error),
                showDialog = true,
                title = errorState.value?.title,
                message = null,
                errorCode = errorState.value?.code,
                errorMessage = errorState.value?.message,
                borderColor = borderColor,
                confirmButtonText = "OK",
                onConfirm = {
                    (context as? Activity)?.finishAffinity()
                    ErrorHandler.clearError()
                },
                dismissButtonText = null,
                onDismiss = onDismiss,
            )

        }*/
    }
}