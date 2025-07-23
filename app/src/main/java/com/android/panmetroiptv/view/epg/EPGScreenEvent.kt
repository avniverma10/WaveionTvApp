package com.android.panmetroiptv.view.epg

sealed class EPGScreenEvent {
    object provideEPGData : EPGScreenEvent()
    object syncEPGDatabase : EPGScreenEvent()
}