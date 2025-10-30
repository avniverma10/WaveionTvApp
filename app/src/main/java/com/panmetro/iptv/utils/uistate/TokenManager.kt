package com.panmetro.iptv.utils.uistate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.panmetro.iptv.utils.network.NetworkApiCallInterface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
// build.gradle(:app): implementation "androidx.datastore:datastore-preferences:1.1.1"
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.panmetro.iptv.model.jwt.RefreshBody
import com.panmetro.iptv.utils.Constants

val android.content.Context.authStore by preferencesDataStore(name = "auth_store")

object AuthKeys {
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")         // consider EncryptedSharedPreferences if needed
    val SESSION  = stringPreferencesKey("sessionToken")
    val REFRESH  = stringPreferencesKey("refreshToken")
    val EXP_EPOCH = longPreferencesKey("sessionExp")        // optional, if your backend returns expiry
}



class TokenManager(
    private val context: Context,
    private val store: DataStore<Preferences>,
    private val authApi: NetworkApiCallInterface
) {
    private val refreshMutex = Mutex()
    private val SKEW = 20L // seconds before exp to refresh (if you have expiry)

    suspend fun bootstrapCredentials(
        username: String,
        password: String,
        refreshToken: String,
       // sessionToken: String
    ) {
        store.edit {
            it[AuthKeys.USERNAME] = username
            it[AuthKeys.PASSWORD] = password
            it[AuthKeys.REFRESH] = refreshToken
           // it[AuthKeys.SESSION] = sessionToken
            it[AuthKeys.EXP_EPOCH] = 0L
        }
    }

    // ---- getters (suspend) ----
    private suspend fun readString(key: Preferences.Key<String>) =
        store.data.first()[key]

    private suspend fun readLong(key: Preferences.Key<Long>) =
        store.data.first()[key] ?: 0L

    suspend fun currentSessionToken(): String? = readString(AuthKeys.SESSION)
    suspend fun currentRefreshToken(): String? = readString(AuthKeys.REFRESH)

    // ---- blocking versions for OkHttp ----
    fun currentSessionTokenBlocking(): String? = runBlocking { currentSessionToken() }

    // Decide if we should proactively refresh (only if you track expiry)
    private fun shouldRefresh(expEpoch: Long): Boolean {
        if (expEpoch <= 0) return false // unknown — rely on 401 path
        val now = System.currentTimeMillis() / 1000
        return expEpoch <= (now + SKEW)
    }

    // Force refresh (401 path). Returns new session token or null on failure
    fun refreshBlocking(): String? = runBlocking {
        refreshMutex.withLock {
            val prefs = store.data.first()
            val username = prefs[AuthKeys.USERNAME] ?: return@withLock null
            val password = prefs[AuthKeys.PASSWORD] ?: return@withLock null
            val oldRefresh = prefs[AuthKeys.REFRESH] ?: return@withLock null
            val oldSession = prefs[AuthKeys.SESSION] ?: ""

            return@withLock try {
                val resp = authApi.refresh(
                    url = Constants.BASE_URL+"",
                    body = RefreshBody(
                        username = username,
                        password = password,
                        refreshToken = oldRefresh
                      //  sessionToken = oldSession
                    )
                )
                store.edit {
                  //  it[AuthKeys.SESSION] = resp.sessionToken
                    it[AuthKeys.REFRESH] = resp.tokens.refreshToken
                    if (resp.tokens.refreshExpiresIn != null) {
                        val newExp = (System.currentTimeMillis() / 1000) + resp.tokens.refreshExpiresIn.toLong()
                        it[AuthKeys.EXP_EPOCH] = newExp
                    }
                }
                resp.tokens.refreshToken
            } catch (_: Exception) {
                // hard logout: clear tokens on failure
                store.edit { it.clear() }
                null
            }
        }
    }

    suspend fun clear() { store.edit { it.clear() } }
}
