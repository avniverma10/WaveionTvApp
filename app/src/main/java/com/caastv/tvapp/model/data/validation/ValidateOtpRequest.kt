package com.caastv.tvapp.model.data.validation

data class ValidateOtpRequest(
    val responseCode: Int,
    val message: String,
    val data: OtpValidationResponse?
)