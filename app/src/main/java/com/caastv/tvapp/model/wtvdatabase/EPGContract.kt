package com.caastv.tvapp.model.wtvdatabase

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

object EPGContract {
    // Instead of using FileProvider, use your own ContentProvider that supports updates
    const val AUTHORITY = "com.android.caastv.provider"
    val BASE_URI: Uri = "content://$AUTHORITY".toUri()
    const val PATH_EPG = "epg"

    object EPGEntry {
        val CONTENT_URI: Uri = BASE_URI.buildUpon().appendPath(PATH_EPG).build()
        const val TABLE_NAME = "epg_data"
        const val COLUMN_ID = "_id"
        const val COLUMN_CHANNEL_ID = "channel_id"
        const val COLUMN_DATA = "data"  // store as JSON String
        const val COLUMN_CHANNEL_HASH = "channel_hash"
        const val COLUMN_LAST_UPDATED = "last_updated"
    }
}
