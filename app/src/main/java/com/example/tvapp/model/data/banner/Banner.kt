package com.example.tvapp.model.data.banner

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Banner(
    val _id: String,
    val name: String,
    val bannerUrl: String,
    val sequence: Int,
    val createdAt: String,
    @SerializedName("__v")
    val version: Int
)
