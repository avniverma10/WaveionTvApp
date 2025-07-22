package com.caastv.tvapp.model.data.login

import com.google.gson.annotations.SerializedName

data class LoginResponseData(
    val code: Int?=null,
    val success: Boolean?=false,
    val message: String?=null,
    @SerializedName("data")
    val loginData: Data?=null
)

data class Data(
    val mobileNo: String,
    val packageExpiryDate: String,
    val packages: List<Package>,
    val userId: String,
    val username: String,
    val regionCode: String?="01"
){
    fun provideUserRegionCode()= regionCode?:"01"
}


data class Channel(
    val ChannelID: String,
    val _id: String,
    val channelNo: Int,
    val title: String
)

data class Package(
    val channels: List<Channel>,
    val deviceLimit: Int,
    val id: String,
    val maxVideoQuality: String,
    val packageId: String,
    val packageName: String,
    val price: Int
)