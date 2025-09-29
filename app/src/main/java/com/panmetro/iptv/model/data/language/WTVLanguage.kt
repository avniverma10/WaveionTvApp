package com.panmetro.iptv.model.data.language

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WTVLanguage(
    val _id:String?="",
    val name:String?="",
    val published:Boolean=false,
    @SerializedName("__v")
    val version:Int=0
)