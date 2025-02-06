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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen() {
    var phoneNumber by remember { mutableStateOf("") }
    var isButtonVisible by remember { mutableStateOf(false) }

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
            }, isButtonVisible)
        }
    }
}

@Composable
fun SendOTP(modifier: Modifier = Modifier) {
    Button(
        onClick = { /* Implement send OTP action here */ },
        modifier = Modifier
            .padding(2.dp)
            .size(90.dp, 30.dp)
    ) {
        Text(text = "Send OTP", fontSize = 8.sp, color = Color.White)
    }
}

@Composable
fun ORSeparator() {
    Column(
        modifier = Modifier
            .height(400.dp) // Adjust height to match QR & Number Pad
            .width(40.dp), // Thin vertical line
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top part of the vertical line
        Box(
            modifier = Modifier
                .weight(1f) // Make it take half the space
                .width(2.dp)
                .background(Color.Gray)
        )

        // OR Text with a clear background
        Box(
            modifier = Modifier
                .background(Color(0xFF0A0E1A)) // Same as screen background
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .width(440.dp)
        ) {
            Text(
                text = "OR",
                color = Color.White, // Change to White for visibility
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom part of the vertical line
        Box(
            modifier = Modifier
                .weight(1f) // Make it take half the space
                .width(2.dp)
                .background(Color.Gray)
        )
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PhoneNumberSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isButtonVisible: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
            modifier = Modifier
                .width(220.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("+91 Enter Number", color = Color.Gray) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(12.dp))
        NumberPad(phoneNumber, onPhoneNumberChange)
        Spacer(modifier = Modifier.height(12.dp))

        if (isButtonVisible) {
            SendOTP()
        }
    }
}

@Composable
fun NumberPad(phoneNumber: String, onPhoneNumberChange: (String) -> Unit) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column {
        numbers.forEach { row ->
            Row {
                row.forEach { digit ->
                    Button(
                        onClick = {
                            when (digit) {
                                "⌫" -> if (phoneNumber.isNotEmpty()) onPhoneNumberChange(phoneNumber.dropLast(1))
                                else -> if (phoneNumber.length < 10) onPhoneNumberChange(phoneNumber + digit)
                            }
                        },
                        modifier = Modifier
                            .padding(6.dp)
                            .size(50.dp),
                        colors = ButtonDefaults.buttonColors(Color.DarkGray)
                    ) {
                        Text(text = digit, fontSize = 22.sp, color = Color.White)
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
@Preview(
    showBackground = true,
    widthDp = 960,
    heightDp = 540
)
@Composable
fun PreviewTvLoginScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen()
        }
    }
}
