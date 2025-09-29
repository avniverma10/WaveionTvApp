package com.panmetro.iptv.utils.network

interface ApiStatusObserver {
    fun onApiStatusChanged(isApiWorking: Boolean)
}