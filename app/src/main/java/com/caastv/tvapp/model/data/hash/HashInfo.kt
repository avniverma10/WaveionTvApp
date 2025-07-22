package com.caastv.tvapp.model.data.hash

import androidx.annotation.Keep

@Keep
data class HashInfo(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val hash: String,
    val username: String
)