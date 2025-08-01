package com.tccl.tvapp.utils.uistate

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.tccl.tvapp.model.data.epgdata.EPGDataItem
import com.tccl.tvapp.model.data.login.LoginResponseData
import com.google.gson.Gson
import com.tccl.tvapp.model.data.settings.AppSettings

object PreferenceManager {
  private lateinit var prefs: SharedPreferences
  private val gson = Gson()

  // Keys
  private const val KEY_GENRE          = "selectedGenreIndex"
  private const val KEY_CHANNEL        = "selectedChannelIndex"
  private const val KEY_PLAYER_CHANNEL = "playerChannelIndex"
  private const val KEY_USERNAME   = "username"
  private const val KEY_PASSWORD   = "password"
  private const val KEY_USER_INFO   = "userinfo"
  private const val KEY_USER_HASH   = "userhash"
  private const val KEY_RECENTS     = "recently_watched"
  private const val KEY_APP_SETTINGS     = "appSettings"
  private const val KEY_PREF_AUDIO    = "preferred_audio"
  private const val KEY_PREF_SUBTITLE = "preferred_subtitle"
  private const val KEY_PREF_VIDEO_QUALITY = "preferred_video_quality"

  /** Must be called once in your Application or Activity */
  fun init(context: Context) {
    prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
  }

  var selectedGenreIndex: Int
    get() = prefs.getInt(KEY_GENRE, 0)
    set(v) = prefs.edit() { putInt(KEY_GENRE, v) }

  var selectedChannelIndex: Int
    get() = prefs.getInt(KEY_CHANNEL, 0)
    set(v) = prefs.edit() { putInt(KEY_CHANNEL, v) }


  /** Persist the last‐seen EPGDataItem as JSON */
  var lastEpgDataItem: EPGDataItem?
    get() {
      val json = prefs.getString(KEY_PLAYER_CHANNEL, null) ?: return null
      return try {
        gson.fromJson(json, EPGDataItem::class.java)
      } catch (e: Exception) {
        null
      }
    }
    set(item) {
      val editor = prefs.edit()
      if (item == null) {
        editor.remove(KEY_PLAYER_CHANNEL)
      } else {
        val json = gson.toJson(item)
        editor.putString(KEY_PLAYER_CHANNEL, json)
      }
      editor.apply()
    }

  /** Save recently watched channels */
  var recentChannelIds: List<String>
    get() {
      return try {
        prefs.getStringSet(KEY_RECENTS, emptySet())!!.toList()
      } catch (e: ClassCastException) {
        // A legacy String was stored here—clear it and fall back
        prefs.edit { remove(KEY_RECENTS) }
        emptyList()
      }
    }
    set(ids) {
      prefs.edit {
        putStringSet(KEY_RECENTS, ids.toSet())
      }
    }


  /**
   * The user’s preferred audio track language (e.g. “en”, “hi”).
   * If null, no override is applied and the player’s default audio is used.
   */
  var preferredAudio: String?
    get() = prefs.getString(KEY_PREF_AUDIO, null)
    set(v) = prefs.edit {
      if (v == null) remove(KEY_PREF_AUDIO)
      else           putString(KEY_PREF_AUDIO, v)
    }

  /**
   * The user’s preferred subtitle track language.
   * If null, subtitles are turned off.
   */
  var preferredSubtitle: String?
    get() = prefs.getString(KEY_PREF_SUBTITLE, null)
    set(v) = prefs.edit {
      if (v == null) remove(KEY_PREF_SUBTITLE)
      else           putString(KEY_PREF_SUBTITLE, v)
    }

  /**
   * The user’s preferred video quality label (e.g. “720p”, “1080p”).
   * If null, Auto (adaptive) quality is used.
   */
  var preferredVideoQuality: String?
    get() = prefs.getString(KEY_PREF_VIDEO_QUALITY, null)
    set(v) = prefs.edit {
      if (v == null) remove(KEY_PREF_VIDEO_QUALITY)
      else           putString(KEY_PREF_VIDEO_QUALITY, v)
    }

  fun clearOptionsTrack(){
    preferredAudio = null
    preferredSubtitle = null
    preferredVideoQuality = null
  }


  /** Save username & password atomically */
  fun saveLogin(username: String, password: String) {
    val editor = prefs.edit()
    editor.putString(KEY_USERNAME, username)
    editor.putString(KEY_PASSWORD, password)
    editor.apply()
  }

  /** Save username & password atomically */
  fun saveUserInfo(userInfo: LoginResponseData) {
    val json = Gson().toJson(userInfo)
    val editor = prefs.edit()
    editor.putString(KEY_USER_INFO, json)
    editor.apply()
  }


  fun saveAppSettings(appSettings: AppSettings) {
    val json = Gson().toJson(appSettings)
    val editor = prefs.edit()
    editor.putString(KEY_APP_SETTINGS, json)
    editor.apply()
  }


  fun getAppSettings(): AppSettings? {
    val json = prefs.getString(KEY_APP_SETTINGS, null)
      ?: return null
    return Gson().fromJson(json, AppSettings::class.java)
  }


  fun saveHash(hash:String) {
    val editor = prefs.edit()
    editor.putString(KEY_USER_HASH, hash)
    editor.apply()
  }


  fun getLoginResponse(): LoginResponseData? {
    val json = prefs.getString(KEY_USER_INFO, null)
      ?: return null
    return Gson().fromJson(json, LoginResponseData::class.java)
  }


  /** Clear only the login keys */
  fun clearLogin(): Boolean {
    val editor = prefs.edit()
    editor.remove(KEY_USER_INFO)
    editor.remove(KEY_USERNAME)
    editor.remove(KEY_PASSWORD)
    editor.remove(KEY_USER_HASH)
    return editor.commit()
  }

  /** Clear recently watched */
  fun clearRecentlyWatched() {
    prefs.edit { remove(KEY_RECENTS) }
  }

  /** Helpers to read them back */
  fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
  fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)
  fun provideUserHash(): String? = prefs.getString(KEY_USER_HASH, null)
}