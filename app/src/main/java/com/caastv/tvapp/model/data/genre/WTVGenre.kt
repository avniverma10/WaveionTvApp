package com.caastv.tvapp.model.data.genre

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WTVGenre(
    val _id: String,
    val name: String,
    val published: Boolean,
    @SerializedName("__v")
    val version: Int,
    @SerializedName("CustomIconUrl")
    val customIconUrl: String?,
    val defaultIcon: String
)