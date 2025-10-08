package com.panmetro.iptv.model.data.genre

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class WTVGenre(
    val _id:String?="",
    var name:String?="",
    val defaultIcon:String?=null,
    val customIconUrl:String?=null,
    val sequence: Int?=null,
    val published:Boolean?=false,
    @SerializedName("__v")
    val version:Int?=0
)