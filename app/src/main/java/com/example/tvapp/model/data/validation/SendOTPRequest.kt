package com.example.tvapp.model.data.validation

data class SendOTPRequest(
    val `data`: SendOTPResponse,
    val message: String,
    val responseCode: Int
)