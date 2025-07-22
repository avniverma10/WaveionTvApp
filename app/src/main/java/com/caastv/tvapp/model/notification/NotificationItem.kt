package com.caastv.tvapp.model.data.notification

import com.google.gson.annotations.SerializedName

data class NotificationItem(
    @SerializedName("_id")      val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("message")  val message: String
)