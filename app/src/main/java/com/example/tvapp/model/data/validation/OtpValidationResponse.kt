package com.example.tvapp.model.data.validation

import androidx.annotation.Keep


@Keep
data class OtpValidationResponse(
    val verificationId: Int,
    val mobileNumber: String,
    val verificationStatus: String,
    val responseCode: Int,
    val errorMessage: String?,
    val transactionId: String?,
    val authToken: String?
)
