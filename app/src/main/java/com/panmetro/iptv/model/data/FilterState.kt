package com.panmetro.iptv.model.data

import androidx.annotation.Keep

@Keep
data class FilterState(
    val genre: String? = null,
    val language: String? = null
//    val country: String? = null,
//    val sortOrder: String? = null
)
