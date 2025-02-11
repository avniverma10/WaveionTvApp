package com.example.tvapp.models

data class SendOTPResponse(
    val mobileNumber: String,
    val responseCode: String,
    val timeout: String,
    val transactionId: String,
    val verificationId: String
)