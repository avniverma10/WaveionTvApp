package com.panmetro.iptv.model.data.health

import androidx.annotation.Keep

@Keep
data class HealthAPIResponse(
    val status: String?=null,
    val timestamp: String?=null
)