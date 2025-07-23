package com.android.panmetroiptv.model.data.appupdate

import androidx.annotation.Keep

@Keep
data class AppUpdateResponse(
    val message: String,
    val data: AppUpdateData
)