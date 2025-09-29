package com.panmetro.iptv.model.data.validation

import androidx.annotation.Keep

@Keep
data class SendOTPResponse(
    val mobileNumber: String,
    val responseCode: String,
    val timeout: String,
    val transactionId: String,
    val verificationId: String
)