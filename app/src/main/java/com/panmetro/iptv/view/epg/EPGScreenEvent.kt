package com.panmetro.iptv.view.epg

sealed class EPGScreenEvent {
    object provideEPGData : EPGScreenEvent()
    object syncEPGDatabase : EPGScreenEvent()
}