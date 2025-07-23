package com.android.panmetroiptv.model.data

import androidx.annotation.Keep

@Keep
data class WTVMediaItemInfo(
    var videoId: String?="",
    var title: String="",
    var description: String="",
    var videoUrl: String="",
    var videoThumbUrl: String?="")

