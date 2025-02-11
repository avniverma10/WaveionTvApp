package com.example.tvapp.models

data class ValidateOtpRequest(
    val responseCode: Int,
    val message: String,
    val data: OtpValidationResponse?
)