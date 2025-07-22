package com.caastv.tvapp.model.data.message

import com.google.gson.annotations.SerializedName

data class ScrollMessageInfo(
    @SerializedName("__v")
    val version: Int?=null,
    val _id: String?=null,
    val backgroundColorHex: String?="#ffffff",
    val backgroundTransparency: String?=".5",
    val createdAt: String?=null,
    val enabled: Boolean?=null,
    val fontColorHex: String?="#000000",
    val fontFamily: String?=null,
    val fontSizeDp: Int?=12,//min 12 and max 50-60
    val fontTransparency: String?=".5",
    val id: String?=null,
    val message: String?=null,
    val messageName: String?=null,
    val posXPercent: Float?=null,
    val posYPercent: Float?=null,
    val positionMode: String?="RANDOM", // "FIXED" or "RANDOM",
    val repeatCount: Int?=5,
    val randomIntervalSec: Int?=10,//for random case handling required it default is 10
    val scrollSpeed: Float?=null,
    val updatedAt: String?=null,
    val messageScope: String?="GLOBAL"
)

