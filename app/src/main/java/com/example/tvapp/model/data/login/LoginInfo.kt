package com.example.tvapp.model.data.login

import androidx.annotation.Keep

@Keep
data class LoginInfo(
    val pkgdata: Pkgdata?= Pkgdata(),    // default to an “empty” PkgData
    val returncode: String,
    val returnmessage: String,
    val regionCode: String="01"//"${RegionCode()}"
){
    fun provideUserRegionCode()= regionCode?:"01"
}



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