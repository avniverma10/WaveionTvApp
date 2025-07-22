package com.caastv.tvapp.model.data.home

import com.google.gson.annotations.SerializedName

data class HomeContent(
    @SerializedName("_id")
    val _id: String,
    val title: String,
    val description: String,
    val contentType: String,
    val categoryId: String,
    val videoUrl: String,
    val duration: Double,
    val releaseDate: String,
    val genreId: String,
    val languageId: String,
    val thumbnailUrl: String,
    val published: Boolean,
    @SerializedName("__v")
    val __v: Double,
    val channelNo: Double,
    @SerializedName("ChannelID")
    val ChannelID: String,
)


data class HomeData(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("filename")
    val fileName: String,
    val lastUpdated: String,
    val url: String,
    val channelId: String,
    val displayName: String,
    val epgFileId: String,
    val content: HomeContent
    )
