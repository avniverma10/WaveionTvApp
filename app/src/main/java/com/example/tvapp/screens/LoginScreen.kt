import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.tv.material3.Text
import com.example.tvapp.viewmodels.OtpViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun LoginScreen(navController: NavController,viewModel: OtpViewModel = hiltViewModel()) {
    var phoneNumber by remember { mutableStateOf("") }
    var isButtonVisible by remember { mutableStateOf(false) }
    var isOTPScreenVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }
    val authToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJDLTRFNDk5QTE3MDcxMzRFMSIsImlhdCI6MTczODgyMDkxMSwiZXhwIjoxODk2NTAwOTExfQ.JL1lWZ8-MfS1x9XWZNxE7KXUn6eLnww6I0JlLEPx9d8CzwpXRQYfA3q571odLNLzQ_kbVUg-71M8TXThp4GEfA"


    if (isOTPScreenVisible) {
        OTPScreen(navController,viewModel, phoneNumber, authToken)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login or Sign Up",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan QR code or enter phone number",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QRCodeSection()
                ORSeparator()
                PhoneNumberSection(phoneNumber, onPhoneNumberChange = {
                    phoneNumber = it
                    isButtonVisible = phoneNumber.length == 10
                }, isButtonVisible = isButtonVisible) {
                    // Call the sendOtp function from ViewModel
                    viewModel.sendOtp(
                        authToken = authToken,
                        phoneNumber = phoneNumber,
                        onSuccess = { isOTPScreenVisible = true },
                        onFailure = { showError = it }
                    )
                }
            }
            if (showError.isNotEmpty()) {
                Text(showError, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SendOTPButton(onSendOtpClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onSendOtpClick() },
        modifier = Modifier
            .padding(6.dp)
            .size(200.dp, 60.dp)
    ) {
        Text(text = "Send OTP", fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun OTPScreen(
    navController: NavController,
    viewModel: OtpViewModel,
    phoneNumber: String,
    authToken: String
) {
    var otpValue by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP sent to +91$phoneNumber",
            color = Color.White,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val digit = otpValue.getOrNull(i)?.toString() ?: ""
                    Text(digit, color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NumberPad(otpValue, onInputChange = { input ->
            if (input.length <= 4) otpValue = input // Limit OTP to 4 digits
        })
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Resend OTP in 00:00",
            color = Color.LightGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Submit OTP Button
        Button(
            onClick = {
                if (otpValue.length == 4) {
                    viewModel.validateOtp(
                        otpCode = otpValue,
                        authToken,
                        onSuccess = {
                        showSuccess = true
                            navController.navigate("player") {
                                popUpTo("login_screen") { inclusive = true } // Remove from backstack
                            }
                    }, onFailure = {
                        showError = it
                    })
                } else {
                    showError = "Please enter a valid 4-digit OTP."
                }
            },
            modifier = Modifier
                .size(200.dp, 60.dp)
                .clip(RoundedCornerShape(10.dp)),
//            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
        ) {
            Text("Submit OTP", color = Color.Black, fontSize = 18.sp)
        }

        if (showError.isNotEmpty()) {
            Text(showError, color = Color.Red, fontSize = 14.sp)
        }

        if (showSuccess) {
            Text("OTP Verified Successfully!", color = Color.Green, fontSize = 16.sp)
        }
    }
}

@Composable
fun QRCodeSection() {
    val qrBitmap = generateQRCode("https://your-login-url.com")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 75.dp)
    ) {
        qrBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(10.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Scan QR to Login", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun ORSeparator() {
    Column(
        modifier = Modifier
            .height(400.dp)
            .width(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(2.dp)
                .background(Color.Gray)
        )
        Box(
            modifier = Modifier
                .background(Color(0xFF0A0E1A))
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .width(440.dp)
        ) {
            Text(
                text = "OR",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .width(2.dp)
                .background(Color.Gray)
        )
    }
}

@Composable
fun PhoneNumberSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isButtonVisible: Boolean,
    onSendOtpClick: () -> Unit
) {
    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // FocusRequester to prevent showing the keyboard
        val focusRequester = FocusRequester()

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
            modifier = Modifier
                .width(220.dp)
                // Disable the keyboard by not requesting focus
                .focusRequester(focusRequester)
                .onFocusChanged {
                    keyboardController?.hide() // Hide the keyboard if it shows up
                },
            placeholder = { Text("+91 Enter Number", color = Color.Gray) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),  // You still specify it here but we don't show the keyboard
            readOnly = true  // Disable interaction with the software keyboard
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Use your custom number pad instead of the default keyboard
        NumberPad(phoneNumber, onPhoneNumberChange)

        Spacer(modifier = Modifier.height(12.dp))

        if (isButtonVisible) {
            SendOTPButton(onSendOtpClick = onSendOtpClick)
        }
    }
}


@Composable
fun NumberPad(input: String, onInputChange: (String) -> Unit) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        numbers.forEach { row ->
            Row {
                row.forEach { digit ->
                    Button(
                        onClick = {
                            when (digit) {
                                "⌫" -> if (input.isNotEmpty()) onInputChange(input.dropLast(1))
                                else -> if (input.length <= 10) onInputChange(input + digit) // Limit OTP to 4 digits
                            }
                        },
                        modifier = Modifier
                            .padding(6.dp)
                            .size(50.dp),
                        colors = ButtonDefaults.buttonColors(Color.DarkGray),
                        contentPadding = PaddingValues(0.dp)
                    )
                    {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center // Centers text in the circle
                        ) {
                            Text(text = digit, fontSize = 22.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: WriterException) {
        Log.e("QRCode", "Error generating QR Code", e)
        null
    }
}
//@Preview(
//    showBackground = true,
//    widthDp = 960,
//    heightDp = 540
//)
//@Composable
//fun PreviewTvLoginScreen() {
//    MaterialTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            LoginScreen()
//        }
//    }
//}
