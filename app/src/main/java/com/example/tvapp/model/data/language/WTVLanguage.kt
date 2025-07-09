package com.example.tvapp.model.data.language

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class WTVLanguage(
    val _id: String,
    val name: String,
    val published: Boolean,
    @SerializedName("__v")
    val version: Int,
    val CustomIconUrl: String?,
    val defaultIcon: String
)
