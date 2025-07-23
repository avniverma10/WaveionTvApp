package com.android.panmetroiptv.extensions

import com.android.panmetroiptv.R
import java.util.Locale



fun String.provideLandingResource():Int {
    return when (this.toLowerCase(Locale.ROOT)) {
        "profile" -> R.drawable.profile
        "search" -> R.drawable.search
        "channels" -> R.drawable.tv
        "settings", "setting" -> R.drawable.settings
        else -> R.drawable.star
    }
}