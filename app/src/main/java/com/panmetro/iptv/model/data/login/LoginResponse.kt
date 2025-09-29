package com.panmetro.iptv.model.data.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("customer-id")
    val customerId: String?=null,
    @SerializedName("customer-number")
    val customerNumber: String,
    val pkgdata: List<String>,//Pkgdata(),    // default to an “empty” PkgData
    val regioncode: String,
    val returncode: String,
    val returnmessage: String,
    @SerializedName("user-id")
    val userId: Int,
    @Volatile
    var drmInfo: DRMUserInfo?=null
)
