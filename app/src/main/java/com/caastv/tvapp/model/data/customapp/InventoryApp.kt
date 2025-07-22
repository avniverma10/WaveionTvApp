package com.caastv.tvapp.model.data.customapp

import com.google.gson.annotations.SerializedName

data class InventoryApp(
    @SerializedName("_id")
    val id: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("playstoreUrl")
    val playstoreUrl: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("iconFileName")
    val iconFileName: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("__v")
    val version: Int
)