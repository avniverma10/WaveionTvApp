package com.caastv.tvapp.model.data.appupdate

import androidx.annotation.Keep

@Keep
data class AppUpdateData(
    val _id: String,
    val appVersion: String,
    val apkUrl: String,
    val updateDate: String,
    val forceUpdate: Int
)