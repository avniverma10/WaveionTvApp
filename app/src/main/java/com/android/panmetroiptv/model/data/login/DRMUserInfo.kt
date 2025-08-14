package com.android.panmetroiptv.model.data.login

import com.google.gson.annotations.SerializedName

data class DRMUserInfo(
    @SerializedName("customer-number")
    val customerNumber: String,
    val id: Int,
    @SerializedName("maximum-assigned-devices")
    val maximumAssignedDevices: Int,
    @SerializedName("maximum-assigned-devices")
    val maximumOfflineAssignedDevices: Int,
    @SerializedName("parental-pin")
    val parentalPin : Int,
    val username: String
)