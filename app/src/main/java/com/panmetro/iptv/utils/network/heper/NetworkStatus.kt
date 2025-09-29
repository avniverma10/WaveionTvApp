package com.panmetro.iptv.utils.network.heper

sealed class NetworkStatus {
    object Available   : NetworkStatus()
    object Unavailable : NetworkStatus()
}