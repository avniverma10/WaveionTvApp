package com.example.tvapp.screens
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Text
import com.example.tvapp.R
import com.example.tvapp.components.GradientBackground
import com.example.tvapp.ui.theme.LightGreen
import com.example.tvapp.ui.theme.grayish_blue
import com.example.tvapp.utils.Constants
import com.example.tvapp.viewmodels.LoginViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    var phoneNumber by remember { mutableStateOf("") }
    var isButtonVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientBackground()  // Apply the gradient background

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title Section
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(start = 128.dp, top = 32.dp, end = 128.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Login or Signup to continue",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_medium)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scan QR Code or enter mobile number to login ",

                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_medium)),
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Content Section with QR, OR, and Mobile Input
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    // QR Code Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        QRCodeSection()
                    }

                    // OR Separator with Images
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.seperator_top),
                            contentDescription = "QR Separator Line Top",
                            modifier = Modifier.height(120.dp), // Adjust height to match the second image
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OR",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFC3C9CC),
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Image(
                            painter = painterResource(id = R.drawable.seperator_bottom),
                            contentDescription = "QR Separator Line Bottom",
                            modifier = Modifier.height(130.dp), // Adjust height to match the second image
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Mobile Number Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PhoneNumberSection(
                            phoneNumber,
                            onPhoneNumberChange = {
                                phoneNumber = it
                                isButtonVisible = phoneNumber.length == 10
                            },
                            isButtonVisible = isButtonVisible
                        ) {
                            viewModel.sendOtp(
                                authToken = Constants.AUTH_TOKEN,
                                phoneNumber = phoneNumber,
                                onSuccess = { /* Handle Success */ },
                                onFailure = { showError = it }
                            )
                        }
                    }
                }
            }

            if (showError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(showError, color = Color.Red, fontSize = 14.sp)
            }
        }
    }
}
@Composable
fun QRCodeSection() {
    val qrBitmap = generateQRCode("https://your-login-url.com")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Use Camera App to Scan QR",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Click on the link generated to redirect to the application",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .width(410.dp)  // Adjust this to control where it breaks into two lines
                .padding(horizontal = 8.dp), // Adds spacing to avoid text clipping
            softWrap = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        qrBitmap?.let {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 47.36.dp,
                        spotColor = Color(0x40FFFFFF),
                        ambientColor = Color(0x40FFFFFF)
                    )
                    .size(200.dp)
                    .background(Color.White, shape = RoundedCornerShape(18.944.dp)),
//                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize() // Ensures QR takes full space
                )
            }
        }
    }
}

@Composable
fun PhoneNumberSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isButtonVisible: Boolean,
    onSendOtpClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding() // **Moves UI up when keyboard opens**
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Enter your Mobile number",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Phone Number Input Field with Updated Color and Proper Keyboard Handling
        OutlinedTextField(
            value = phoneNumber,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.Gray,
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor =  Color(0xFF2A2A2A),
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                cursorColor = LightGreen,
                focusedIndicatorColor = LightGreen,
                unfocusedIndicatorColor = Color.Transparent
            ),
            onValueChange = { input ->
                if (input.length <= 10 && input.all { it.isDigit() }) {
                    onPhoneNumberChange(input)
                }
            },
            textStyle = TextStyle(color = Color.White, fontSize = 18.sp), // **Updated Text Color**
            placeholder = {
                Text("+91 Mobile number", color = Color.Gray)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .width(280.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show() // Show keyboard when focused
                    }
                }
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show() // Show keyboard on click
                }
        )

        Spacer(modifier = Modifier.height(12.dp)) // Space before keyboard appears

        // Button appears below the keyboard naturally
        if (isButtonVisible) {
            Button(
                onClick = {
                    keyboardController?.hide() // Hide keyboard when clicking send OTP
                    focusManager.clearFocus() // Remove focus to close keyboard
                    onSendOtpClick()
                },
                modifier = Modifier.width(220.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF999999))
            ) {
                Text("Send OTP", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun PreviewLoginScreen() {
    val navController = rememberNavController()
    LoginScreen(navController)
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

fun generateQRCode(content: String, size: Int = 1024): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.TRANSPARENT)
            }
        }

        return Bitmap.createScaledBitmap(bitmap, size, size, false) // Scale QR to fit
    } catch (e: WriterException) {
        Log.e("QRCode", "Error generating QR Code", e)
        null
    }
}




