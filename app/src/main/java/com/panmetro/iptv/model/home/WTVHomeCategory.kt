package com.panmetro.iptv.model.home

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WTVHomeCategory(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val channels: List<String>,
    val order: Int,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v")
    val version: Int
)