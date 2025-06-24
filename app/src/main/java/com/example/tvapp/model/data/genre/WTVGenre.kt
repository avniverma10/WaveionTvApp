package com.example.tvapp.model.data.genre

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WTVGenre(
    val _id:String?="",
    var name:String?="",
    val published:Boolean?=false,
    @SerializedName("__v")
    val version:Int?=0
)