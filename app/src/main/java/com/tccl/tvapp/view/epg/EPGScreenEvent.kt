package com.tccl.tvapp.view.epg

sealed class EPGScreenEvent {
    object provideEPGData : EPGScreenEvent()
    object syncEPGDatabase : EPGScreenEvent()
}