package com.example.tvapp.model.notification

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NotificationItem(
    @SerializedName("_id")      val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("message")  val message: String
)