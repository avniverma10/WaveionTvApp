package com.tccl.tvapp.model.data.customapp

import androidx.annotation.DrawableRes

data class AppItem(
    val displayName: String,
    @DrawableRes val logoRes: Int ,
    val url: String,
    val packageName: String

)