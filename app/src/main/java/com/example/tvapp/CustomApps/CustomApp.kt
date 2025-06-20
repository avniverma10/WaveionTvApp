package com.example.tvapp.CustomApps

import androidx.annotation.DrawableRes

// 1) Your data model
data class AppItem(
    val displayName: String,
    @DrawableRes val logoRes: Int ,
    val url: String,
    val packageName: String

)