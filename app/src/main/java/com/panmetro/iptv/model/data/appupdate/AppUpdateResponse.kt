package com.panmetro.iptv.model.data.appupdate

import androidx.annotation.Keep

@Keep
data class AppUpdateResponse(
    val message: String,
    val data: AppUpdateData
)