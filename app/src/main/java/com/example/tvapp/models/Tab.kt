package com.example.tvapp.models

import com.google.gson.annotations.SerializedName

data class Tab(
    val __v: Int,
    @SerializedName("_id") val id: String,
    val components: List<Component>,
    val displayName: String,
    val isVisible: Boolean,
    val iconUrl: String? = null,
    val name: String
)