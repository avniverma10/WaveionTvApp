package com.caastv.tvapp.extensions

import com.android.caastv.R
import java.util.Locale


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