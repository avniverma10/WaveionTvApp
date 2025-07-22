package com.caastv.tvapp.model.data.banner

import com.google.gson.annotations.SerializedName

data class Banner(
    val _id: String,
    val name: String,
    val bannerUrl: String,
    val bannerContentLink: String,
    val sequence: Int,
    val createdAt: String,
    @SerializedName("__v")
    val version: Int
)