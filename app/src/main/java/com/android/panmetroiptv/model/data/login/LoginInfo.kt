package com.android.panmetroiptv.model.data.login

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// Matches your actual API response:
data class LoginApiResponse(
    @SerializedName("returnmessage") val message: String,
    @SerializedName("returncode") val code: String,
    @SerializedName("user-id") val userId: Int,
    @SerializedName("customer-id") val customerId: String,
    @SerializedName("customer-number") val customerNumber: String,
    @SerializedName("regioncode") val regionCode: String,
    @SerializedName("pkgdata") val packages: List<String>
)


@Keep
data class LoginInfo(
    val userId: Int,
    val customerNumber: String,
    val code: String,
    val regionCode: String,
    val packages: List<String>
){
    fun provideUserRegionCode()= regionCode?:"01"
}

// Conversion extension
fun LoginApiResponse.toLoginInfo() = LoginInfo(
    userId = this.userId,
    customerNumber = this.customerNumber,
    code = this.code,
    regionCode = this.regionCode,
    packages = this.packages
)


@Keep
data class Activepack(
    val expirydate: String,
    val servicecode: String,
    val servicename: String
)

@Keep
data class RegionCode(
    val name: String="Region",
    val code: String="01"
)

@Keep
data class Pkgdata(
    val activepackcount: Int = 0,
    val activepack: List<Activepack> = emptyList()
)