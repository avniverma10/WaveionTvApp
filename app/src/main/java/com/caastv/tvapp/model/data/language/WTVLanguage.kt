package com.caastv.tvapp.model.data.language

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WTVLanguage(
    val _id: String,
    val name: String,
    val published: Boolean,
    @SerializedName("__v")
    val version: Int,
    @SerializedName("CustomIconUrl")
    val customIconUrl: String?,
    val defaultIcon: String
)
