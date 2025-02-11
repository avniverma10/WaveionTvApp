package com.example.tvapp.models

data class SendOTPRequest(
    val `data`: SendOTPResponse,
    val message: String,
    val responseCode: Int
)