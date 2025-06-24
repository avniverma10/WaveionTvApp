package com.example.tvapp.model.data.sse

import androidx.annotation.Keep


@Keep
data class TabItem(
    val _id: String,
    val name: String,
    val displayName: String,
    val isVisible: Boolean,
    val components: List<Component>,
    val iconUrl: String,
    val sequence: Int,
    val __v: Int
)


@Keep
data class Component(
    val name: String,
    val isVisible: Boolean?=false,
    val _id: String
)