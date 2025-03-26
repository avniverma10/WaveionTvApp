package com.example.tvapp.model.data.language

import com.google.gson.annotations.SerializedName

data class WTVLanguage(
    val _id:String?="",
    val name:String?="",
    val published:Boolean=false,
    @SerializedName("__v")
    val version:Int=0
)