package com.example.tvapp.model.data.epgdata

import com.google.gson.annotations.SerializedName

data class EPGDataItem(
    val _id: String?="67d27a11140c8a5c710993d6",
    val channelId: String?="ZEE_SALAAM_RS-0.10",
    val content: Content?=null,
    val displayName: String?="ZEE SALAAM RS-0.10",
    val epgFileId: String?="67d27a11140c8a5c710993d6",
    val filename: String?="ZEE SALAAM RS-0.10.XML",
    val lastUpdated: String?="2025-03-13T06:24:17.848Z",
    val tv: Tv?=null,
    val url: String?=null,
    val channelHash: String?=null
)


data class Tv(
    val channel: Channel?=null,
    val programme: List<Programme>?= null
)

data class Programme(
    @SerializedName("_channel")
    val channelId: String?="ZEE_SALAAM_RS-0.10",
    @SerializedName("_clumpidx")
    val clumpIdx: String?="0/1",
    @SerializedName("_start")
    val startTime: String?="20250212000000 +0530",
    @SerializedName("_stop")
    val endTime: String?="20250212003000 +0530",
    val date: String?="20250212",
    val desc: String?="Covering the top highlight of day from across the globe.",
    val title: String?="News Panorama",
    var watchedAt: Long? = null // Add timestamp to track when watched
)

data class Content(
    val ChannelID: String?="67a6ff5b72bb0101dcc82ad4",
    @SerializedName("__v")
    val version: Int?=0,
    val _id: String?="67a6ff5b72bb0101dcc82ad4",
    val categoryId: String?="67a34e7340be795c51077123",
    val channelNo: Int?=98,
    val contentType: String?="4",
    val description: String?="ET",
    val duration: Int?=567,
    val genreId: String?="music",
    val languageId: String?="67a34e4740be795c51077115",
    val published: Boolean?=true,
    val releaseDate: String?="3545-05-31T00:00:00.000Z",
    val thumbnailUrl: String?="https://nextwave.waveiontechnologies.com:5000/uploads/thumbnails/1738997579816.jpg",
    val title: String?="005 News 18 UP",
    val videoUrl: String?="https://nextwave.waveiontechnologies.com:8447/ottproxy/live/disk0/Chardikla_Time_TV/DASH/Chardikla_Time_TV.mpd",
    val genre: List<String>? = emptyList(),
    val language: String?= ""
)
data class Channel(
    val _id: String?="ZEE_SALAAM_RS-0.10",
    @SerializedName("display-name")
    val displayName: String?="ZEE SALAAM RS-0.10",
    val logoUrl: String? = null,
    val videoUrl: String? = null,
    val genreId: String //  genreId for filtering
)