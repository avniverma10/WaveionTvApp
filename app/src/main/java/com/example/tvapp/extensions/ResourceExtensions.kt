package com.example.tvapp.extensions

import com.android.tccl.R
import java.util.Locale



fun String.provideLandingResource():Int {
    return when (this.toLowerCase(Locale.ROOT)) {
        "profile" -> R.drawable.profile
        "search" -> R.drawable.search
        "epg" -> R.drawable.airplay
        "home" -> R.drawable.home
        "channels" -> R.drawable.tv
        "settings", "setting" -> R.drawable.settings
        else -> R.drawable.box
    }
}

fun String.provideCategoryResource():Int{
    return when(this.toLowerCase(Locale.ROOT)){
        "all"-> R.drawable.all
        "recent"-> R.drawable.recent
        "news"-> R.drawable.news
        "face"-> R.drawable.face
        "music"-> R.drawable.music
        "kid","kids"-> R.drawable.kid
        "spirit"-> R.drawable.spirit
        "movie"-> R.drawable.movie
        "star"-> R.drawable.star
        else -> R.drawable.all
    }
}