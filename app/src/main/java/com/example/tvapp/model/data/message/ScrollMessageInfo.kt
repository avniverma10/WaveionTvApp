package com.example.tvapp.model.data.message

import com.google.gson.annotations.SerializedName

data class ScrollMessageInfo(
    @SerializedName("__v")
    val version: Int,
    val _id: String,
    val backgroundColorHex: String,
    val backgroundTransparency: Any,
    val createdAt: String,
    val enabled: Boolean,
    val fontColorHex: String,
    val fontFamily: String,
    val fontSizeDp: Int,
    val fontTransparency: Any,
    val id: String,
    val message: String,
    val messageName: String,
    val posXPercent: Double,
    val posYPercent: Double,
    val positionMode: String,
    val repeatCount: Int,
    val scrollSpeed: Double,
    val updatedAt: String
)