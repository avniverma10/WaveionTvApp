package com.android.panmetroiptv.utils.network.heper

sealed class NetworkStatus {
    object Available   : NetworkStatus()
    object Unavailable : NetworkStatus()
}