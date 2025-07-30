package com.caastv.tvapp.utils.network

interface ApiStatusObserver {
    fun onApiStatusChanged(isApiWorking: Boolean)
}