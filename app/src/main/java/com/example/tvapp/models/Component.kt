package com.example.tvapp.models

import com.google.gson.annotations.SerializedName

data class Component(
    @SerializedName("_id")val id: String,
    val isVisible: Boolean,
    val name: String
)