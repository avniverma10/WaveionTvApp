package com.example.tvapp.repository

import android.util.Log
import com.example.tvapp.models.Tab
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class TabsRepository {
    private val client = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES) // Increase read timeout as necessary
        .build()
    private val gson = Gson()

    fun streamTabs(): Flow<List<Tab>> = callbackFlow {
        connectToSSE(this)  // Pass the callbackFlow scope for SSE connection
        awaitClose { /* Logic to handle closure, potentially cancelling SSE */ }
    }

    private fun connectToSSE(scope: ProducerScope<List<Tab>>) {
        val request = Request.Builder()
            .url("http://nextwave.waveiontechnologies.com:5000/api/tabs/sse-tabs")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                val tabs = parseTabs(data)
                Log.i("RISHI", "onEvent: Tabs updated ${tabs}")
                scope.trySend(tabs).isSuccess
            }

            override fun onClosed(eventSource: EventSource) {
                Log.i("RISHI", "SSE Connection closed")
                scope.close()
            }


            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("RISHI", "SSE Failure: ", t)
                scope.close(t)  // Optionally pass the throwable to close to emit an error
                scheduleReconnect(scope)  // Attempt to reconnect
            }
        }

        EventSources.createFactory(client).newEventSource(request, listener)
    }

    private fun scheduleReconnect(scope: ProducerScope<List<Tab>>) {
        scope.launch {
            delay(10000) // Shorter reconnection delay
            connectToSSE(scope)  // Reconnect using the same flow scope
        }
    }


    private fun parseTabs(data: String): List<Tab> {
        val type = object : TypeToken<List<Tab>>() {}.type
        return gson.fromJson(data, type)
    }
}

