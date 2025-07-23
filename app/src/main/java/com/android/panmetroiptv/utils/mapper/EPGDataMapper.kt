package com.android.panmetroiptv.utils.mapper

import androidx.media3.common.MediaItem
import com.android.panmetroiptv.model.data.WTVMediaItemInfo

fun WTVMediaItemInfo.toMediaItemInfo() =
    WTVMediaItemInfo(
        videoId = videoId?:"",
        title = title?:"",
        description = description?:"",
        videoUrl = videoUrl?:"",
        videoThumbUrl = videoThumbUrl?:""
    )

fun MediaItem.toMediaItemInfo() =
    WTVMediaItemInfo(
        videoId = mediaId,
        title = mediaMetadata.title.toString(),
        description = mediaMetadata.subtitle.toString(),
        videoUrl = mediaId,
        videoThumbUrl  = mediaMetadata.artworkUri.toString()
    )