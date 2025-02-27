package com.example.tvapp.models

data class EpgFile(
    val _id: String,
    val filename: String,
    val lastUpdated: String,
    val url: String,
    val channelId: String,
    val displayName: String,
    val epgFileId: String,
    val content: Content
)

data class Content(
    val _id: String,
    val title: String,
    val description: String,
    val contentType: String,
    val categoryId: String,
    val videoUrl: String,
    val duration: Int,
    val releaseDate: String,
    val genreId: String,
    val languageId: String,
    val thumbnailUrl: String,
    val published: Boolean,
    val __v: Int,
    val channelNo: Long,
    val ChannelID: String
)
