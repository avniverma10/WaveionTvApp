package com.example.tvapp.utils

import android.content.Context
import android.util.Log
import com.example.tvapp.database.UserHash
import com.example.tvapp.database.UserHashDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun saveHashToDatabase(context: Context, phoneNumber: String?, deviceId: String, hash: String) {
    val database = UserHashDatabase.getDatabase(context)
    val userHashDao = database.userHashDao()

    val userHash = UserHash(phoneNumber = phoneNumber, deviceId = deviceId, watermarkHash = hash)

    withContext(Dispatchers.IO) {
        userHashDao.insertUserHash(userHash)
        Log.d("HASH_TRACKING", "Saved hash for user: $phoneNumber, Device: $deviceId, Hash: $hash")
    }
}