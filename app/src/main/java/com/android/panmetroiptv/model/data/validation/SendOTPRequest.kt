package com.android.panmetroiptv.model.data.validation

import androidx.annotation.Keep

@Keep
data class SendOTPRequest(
    val `data`: SendOTPResponse,
    val message: String,
    val responseCode: Int
)