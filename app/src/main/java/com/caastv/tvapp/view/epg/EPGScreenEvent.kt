package com.caastv.tvapp.view.epg

sealed class EPGScreenEvent {
    object provideEPGData : EPGScreenEvent()
    object syncEPGDatabase : EPGScreenEvent()
}