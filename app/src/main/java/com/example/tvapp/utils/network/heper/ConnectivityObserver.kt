package com.example.tvapp.utils.network.heper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.tvapp.extensions.showToastS
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ConnectivityObserver(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.M)
    fun observe(): Flow<NetworkStatus> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //Build a request for any network with INTERNET capability
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        //Create a callback that sends Available/Unavailable
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                //context.showToastS("Available")
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                //context.showToastS("UnAvailable")

                // If we lose a network, check if any others remain. For simplicity, assume Unavailable:
                trySend(NetworkStatus.Unavailable)
            }
        }

        // 3) Register the callback
        connectivityManager.registerNetworkCallback(request, callback)

        // 4) Emit an initial value based on current connectivity
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val initialStatus = if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
        trySend(initialStatus)

        // 5) Clean up when the flow is closed
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}