package com.panmetro.iptv.model.data.epgdata

import androidx.annotation.Keep
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.panmetro.iptv.model.data.genre.WTVGenre
import com.panmetro.iptv.model.data.language.WTVLanguage
import kotlin.math.max

@Keep
data class EPGDataItem(
    @SerializedName("ChannelID")
    val channelId: String?="ZEE_SALAAM_RS-0.10",
    @SerializedName("__v")
    val version: Int?=0,
    val _id: String?="67a6ff5b72bb0101dcc82ad4",
    val channelNo: Int?=98,
    val contentType: String?="4",
    val description: String?="ET",
    val duration: Int?=567,
    val epgDisplayName: String?=null,
    val published: Boolean?=true,
    val releaseDate: String?="3545-05-31T00:00:00.000Z",
    val thumbnailName: String?=null,
    val thumbnailUrl: String?="https://nextwave.waveiontechnologies.com:5000/uploads/thumbnails/1738997579816.jpg",
    val title: String?="005 News 18 UP",
    val videoUrl: String?=null,
    val genreId: String?="music",
    val genre: List<WTVGenre>? = emptyList(),
    val language: WTVLanguage?= null,
    @SerializedName("DRM")
    val drmType: String?=null,
    val assetId: String?=null,
    val streamType: String?=null,
    @SerializedName("bgGradient")
    val bgGradient: BgGradient? = null,
    // val content: Content?=null,
    val displayName: String?="ZEE SALAAM RS-0.10",
    val epgFileId: String?="67d27a11140c8a5c710993d6",
    @SerializedName("epgFilename")
    val filename: String?="ZEE SALAAM RS-0.10.XML",
    @SerializedName("epgLastUpdated")
    val lastUpdated: String?="2025-03-13T06:24:17.848Z",
    val tv: Tv?=null,
    @SerializedName("epgUrl")
    val url: String?=null,
    val channelHash: String?=null,
    val currentPrograms: List<Programme>?=null
)




@Keep
data class Tv(
    val channel: Channel?=null,
    val programme: List<Programme>?= null,
    val epgFormat: String?= null,//MASTER and TYPE1
)
@Keep
data class Programme(
    @SerializedName("_channel")
    val channelId: String?=null,
    @SerializedName("_clumpidx")
    val clumpIdx: String?="0/1",
    @SerializedName("_start")
    @JsonAdapter(ProgramTimestampAdapter::class)
    var startTime: Long? =  null ,//strtTime.toProgramFormattedTime(),
    @SerializedName("_stop")
    @JsonAdapter(ProgramTimestampAdapter::class)
    var endTime: Long? = null ,//stopTime.toProgramFormattedTime(),
    val date: String?=null,
    val desc: String?="No Information",
    val title: String?="No Information",
    @SerializedName("ImageUrl")
    val imageUrl: List<ImageUrl>?=null,
    val director: String?=null,
    val episodenumber: String?=null,
    val genre: Genre?=null,
    val parentalrating: String?=null,
    val producer: String?=null,
    val programmeid: String?=null,
    val releaseyear: String?=null,
    val starcast: String?=null,
    @SerializedName("sub-genre")
    val subGenr: SubGenre?=null,
    val writer: String?=null,
    var watchedAt: Long? = null, // Add timestamp to track when watched
    var isVisible: Boolean = false, // Add timestamp to check current watchableProgram
    @Volatile
    var startFormatedTime:String?=null,
    @Volatile
    var endFormatedTime:String?=null
){
    // Extension functions for Long? timestamps
    fun Long?.calculateDurationInMinutes(endTime: Long?): Long {
        if (this == null || endTime == null) return 0L
        return max(0, (endTime - this) / (60 * 1000)) // Convert milliseconds to minutes
    }

    fun Long?.calculateDurationInHours(endTime: Long?): Double {
        if (this == null || endTime == null) return 0.0
        return max(0.0, (endTime - this) / (60.0 * 60 * 1000)) // Convert milliseconds to hours
    }

    fun Long?.calculateDurationInSeconds(endTime: Long?): Long {
        if (this == null || endTime == null) return 0L
        return max(0, (endTime - this) / 1000) // Convert milliseconds to seconds
    }
    /* init {
         startTime = strtTime.toProgramFormattedTime()
         endTime = stopTime.toProgramFormattedTime()
     }*/

    fun provideExactProgramWidth(): Long{
        return (startTime.calculateDurationInMinutes(endTime)*5)?:150L
    }
}




@Keep
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
    val genre: List<WTVGenre>? = emptyList(),
    val language: WTVLanguage?= null,
    @SerializedName("DRM")
    val drmType: String?="cryptoguard",//{sigma, cryptoguard, None}
    val assetId: String?="546465f1-a54c-4146-b417-c5d5ac0d0802",
    val streamType: String?="",
    @SerializedName("bgGradient")
    val bgGradient: BgGradient? = null
)


@Keep
data class Channel(
    val _id: String?="ZEE_SALAAM_RS-0.10",
    @SerializedName("display-name")
    val displayName: String?="ZEE SALAAM RS-0.10",
    @SerializedName("Category")
    val category: String?=null,
    @SerializedName("channel-desc")
    val channelDesc: String?=null,
    @SerializedName("channel-descshort")
    val channelDescshort: String?=null,
    @SerializedName("ChannelLogo")
    val channelLogo: List<Any>?=null,
    val logoUrl: String? = null,
    val videoUrl: String? = null,
    val genreId: String, //  genreId for filtering
    val channelNo: Int? = null,
    val availableProgramme: List<Programme>?= null,
    val bgGradient: BgGradient? = null
){
    fun provideLogo(size:String?=null):String?{
        size?.let {
            return (channelLogo?.find { logo-> (logo as? ChannelLogoInfo)?.size.equals(size,true) }?:channelLogo?.getOrNull(0)).toString()
        }?:run {
            channelLogo?.getOrNull(0)
        }
        return null
    }
}


@Keep
data class ChannelLogoInfo(
    @SerializedName("_")
    val url: String,
    @SerializedName("_size")
    val size: String
)

@Keep
data class BgGradient(
    val type: String,
    val angle: Int,
    val colors: List<GradientColor>
)


@Keep
data class GradientColor(
    @SerializedName("_id")
    val id: String,
    val color: String,
    val percentage: Int
)

@Keep
data class SubGenre(
    @SerializedName("_")
    val name: String,
    @SerializedName("_lang")
    val lang: String
)
@Keep
data class Genre(
    @SerializedName("_")
    val name: String,
    @SerializedName("_lang")
    val lang: String
)
@Keep
data class ImageUrl(
    @SerializedName("_")
    val name: String,
    @SerializedName("_size")
    val size: String
)