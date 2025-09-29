package com.panmetro.iptv.model.data.validation

import androidx.annotation.Keep

@Keep
data class ValidateOtpRequest(
    val responseCode: Int,
    val message: String,
    val data: OtpValidationResponse?
)