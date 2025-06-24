package com.example.tvapp.utils.network.heper

sealed class NetworkStatus {
    object Available   : NetworkStatus()
    object Unavailable : NetworkStatus()
}