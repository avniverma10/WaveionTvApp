package com.caastv.tvapp.utils.network.heper

sealed class NetworkStatus {
    object Available       : NetworkStatus()
    object Unavailable     : NetworkStatus()
    object Losing          : NetworkStatus() // e.g. about to lose connectivity
    object Lost            : NetworkStatus()
}