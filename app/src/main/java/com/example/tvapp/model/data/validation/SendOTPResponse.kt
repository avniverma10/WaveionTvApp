package com.example.tvapp.model.data.validation

data class SendOTPResponse(
    val mobileNumber: String,
    val responseCode: String,
    val timeout: String,
    val transactionId: String,
    val verificationId: String
)