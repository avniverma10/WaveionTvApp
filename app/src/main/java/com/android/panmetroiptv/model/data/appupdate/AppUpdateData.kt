package com.android.panmetroiptv.model.data.appupdate

import androidx.annotation.Keep

@Keep
data class AppUpdateData(
    val _id: String,
    val appVersion: String,
    val apkUrl: String,
    val updateDate: String,
    val forceUpdate: Int,
    val regions: ArrayList<RegionInfo>?=null
)

data class RegionInfo(val code: String?=null,val name: String?=null,val forceUpdate: Boolean?=false,val _id: String?=null)