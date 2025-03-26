package com.example.tvapp.model.data.genre

import com.google.gson.annotations.SerializedName

data class WTVGenre(
    val _id:String?="",
    val name:String?="",
    val published:Boolean=false,
    @SerializedName("__v")
    val version:Int=0
)