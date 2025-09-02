package com.android.panmetroiptv.utils.network

interface ApiStatusObserver {
    fun onApiStatusChanged(isApiWorking: Boolean)
}