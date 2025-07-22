//package com.caastv.tvapp.utils.network.heper
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.Network
//import android.net.NetworkCapabilities
//import android.net.NetworkRequest
//import android.os.Build
//import androidx.annotation.RequiresApi
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//
//class ConnectivityObserver(context: Context) {
//
//    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
//                 as ConnectivityManager
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    fun observe(): Flow<NetworkStatus> = callbackFlow {
//        val request = NetworkRequest.Builder()
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .build()
//
//        val callback = object : ConnectivityManager.NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                trySend(NetworkStatus.Available)
//            }
//            override fun onUnavailable() {
//                trySend(NetworkStatus.Unavailable)
//            }
//            override fun onLosing(network: Network, maxMsToLive: Int) {
//                trySend(NetworkStatus.Losing)
//            }
//            override fun onLost(network: Network) {
//                trySend(NetworkStatus.Lost)
//            }
//        }
//
//        cm.registerNetworkCallback(request, callback)
//        // Emit current status immediately
//        val active = cm.activeNetwork
//        val caps   = active?.let { cm.getNetworkCapabilities(it) }
//        val initial = when {
//            caps == null                              -> NetworkStatus.Unavailable
//            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkStatus.Available
//            else                                      -> NetworkStatus.Losing
//        }
//        trySend(initial)
//
//        awaitClose { cm.unregisterNetworkCallback(callback) }
//    }
//}
