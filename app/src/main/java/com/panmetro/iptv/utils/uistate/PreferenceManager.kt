package com.panmetro.iptv.utils.uistate

import android.content.Context
import android.content.SharedPreferences
import com.panmetro.iptv.extensions.convertIntoModel
import com.panmetro.iptv.model.data.favorite.Favorite
import com.panmetro.iptv.model.data.login.CustomerPackageInfo
import com.panmetro.iptv.model.data.login.LoginInfo
import com.google.gson.Gson

object PreferenceManager {
  private lateinit var prefs: SharedPreferences
  private val gson = Gson()

  // Keys
  private const val KEY_GENRE          = "genre"
  private const val KEY_CHANNEL        = "channel"
  private const val KEY_USERNAME   = "username"
  private const val KEY_PASSWORD   = "password"
  private const val KEY_CUS_NUMBER   = "customer_number"
  private const val KEY_USER_INFO   = "userinfo"
  private const val KEY_USER_PKG_INFO   = "userPKGInfo"
  private const val KEY_USER_HASH   = "userhash"


  private const val KEY_PLAYER_FINGERPRINT   = "playerFingerprint"
  private const val KEY_GLOBAL_FINGERPRINT   = "globalFingerprint"
  private const val KEY_PLAYER_SCROLL   = "playerScroll"
  private const val KEY_PLAYER_CHANNEL_SCROLL   = "playerChannelScroll"
  private const val KEY_GLOBAL_SCROLL   = "globalScroll"
  private const val KEY_PLAYER_FORCE   = "playerForce"
  private const val KEY_GLOBAL_FORCE   = "globalForce"



  /** Must be called once in your Application or Activity */
  fun init(context: Context) {
    prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
  }

  /** Save genre atomically */
  fun saveGenre(genre: String) {
    val editor = prefs.edit()
    editor.putString(KEY_GENRE, genre)
    editor.apply()
  }
  /** Save genre atomically */
  fun saveChannel(channel: String) {
    val editor = prefs.edit()
    editor.putString(KEY_CHANNEL, channel)
    editor.apply()
  }
  /** Save username & password atomically */
  fun saveLogin(username: String, password: String) {
    val editor = prefs.edit()
    editor.putString(KEY_USERNAME, username)
    editor.putString(KEY_PASSWORD, password)
    editor.apply()
  }

  /** Save username & password atomically */
  fun saveUserPackageInfo(userPkgInfo: CustomerPackageInfo) {
    val json = Gson().toJson(userPkgInfo)
    val editor = prefs.edit()
    editor.putString(KEY_USER_PKG_INFO, json)
    editor.apply()
  }
  fun getUserPackageInfo(): CustomerPackageInfo? {
    val json = prefs.getString(KEY_USER_PKG_INFO, null)
      ?: return null
    return Gson().fromJson(json, CustomerPackageInfo::class.java)
  }


  /** Clear pkg only the*/
  fun clearPkg(): Boolean {
    val editor = prefs.edit()
    editor.remove(KEY_USER_PKG_INFO)
    return editor.commit()
  }

  /** Save username & password atomically */
  fun saveUserInfo(userInfo: LoginInfo) {
    val json = Gson().toJson(userInfo)
    val editor = prefs.edit()
    editor.putString(KEY_USER_INFO, json)
    editor.apply()
  }
  fun getLoginResponse(): LoginInfo? {
    val json = prefs.getString(KEY_USER_INFO, null)
      ?: return null
    return Gson().fromJson(json, LoginInfo::class.java)
  }


  fun saveUserFav(userId: String, favList: Favorite) {
    val json = Gson().toJson(favList)
    val editor = prefs.edit()
    editor.putString(userId, json)
    editor.apply()
  }


  fun getUserFav(username: String): List<String>? {
    val json = prefs.getString(username, null) ?: return null
    val favorite =  json.convertIntoModel(Favorite::class.java)
    return favorite?.channelIds
  }

  /** Clear fav only the*/
  fun clearFav(userId:String): Boolean {
    val editor = prefs.edit()
    editor.remove(userId)
    return editor.commit()
  }

  fun saveHash(hash:String) {
    val editor = prefs.edit()
    editor.putString(KEY_USER_HASH, hash)
    editor.apply()
  }

  fun saveCusNumber(cNum:String) {
    val editor = prefs.edit()
    editor.putString(KEY_CUS_NUMBER, cNum)
    editor.apply()
  }


  /** Clear only the login keys */
  fun clearLogin(): Boolean {
    val editor = prefs.edit()
    editor.remove(KEY_USER_INFO)
    editor.remove(KEY_USER_PKG_INFO)
    editor.remove(KEY_USERNAME)
    editor.remove(KEY_PASSWORD)
    editor.remove(KEY_USER_HASH)
    editor.remove(KEY_CUS_NUMBER)
    return editor.commit()
  }

  /** Clear only the Genre keys */
  fun clearSaveGenre(): Boolean {
    val editor = prefs.edit()
    editor.remove(KEY_GENRE)
    return editor.commit()
  }

  /** Clear only the Genre keys */
  fun clearSaveChannel(): Boolean {
    val editor = prefs.edit()
    editor.remove(KEY_CHANNEL)
    return editor.commit()
  }


  /** Helpers to read them back */
  fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
  fun getCustomerNumber(): String? = prefs.getString(KEY_CUS_NUMBER, null)
  fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)
  fun provideUserHash(): String? = prefs.getString(KEY_USER_HASH, null)

  fun getSavedGenre(): String? = prefs.getString(KEY_GENRE, null)
  fun getSavedChannel(): String? = prefs.getString(KEY_CHANNEL, null)


  //SSE
  /*fun saveGlobalFingerTime(updateAt:String) {
    val editor = prefs.edit()
    editor.putString(KEY_GLOBAL_FINGERPRINT, updateAt)
    editor.apply()
  }
  fun savePlayerFingerTime(updateAt:String) {
    val editor = prefs.edit()
    editor.putString(KEY_PLAYER_FINGERPRINT, updateAt)
    editor.apply()
  }
  fun saveGlobalScrollTime(updateAt:String) {
    val editor = prefs.edit()
    editor.putString(KEY_GLOBAL_SCROLL, updateAt)
    editor.apply()
  }
  fun savePlayerScrollTime(_id:String,updateAt:String) {
    val editor = prefs.edit()
    editor.putString(_id, updateAt)
    editor.apply()
  }
  fun saveGlobalForceTime(updateAt:String) {
    val editor = prefs.edit()
    editor.putString(KEY_GLOBAL_FORCE, updateAt)
    editor.apply()
  }
  fun savePlayerForceTime(updateAt:String) {
    val editor = prefs.edit()
    editor.putString(KEY_PLAYER_FORCE, updateAt)
    editor.apply()
  }*/



  fun saveFingerUpdatedAt(_id:String,updateAt:String) {
    val editor = prefs.edit()
    editor.putString(_id, updateAt)
    editor.apply()
  }

  fun saveScrollUpdatedAt(_id:String,updateAt:String) {
    val editor = prefs.edit()
    editor.putString(_id, updateAt)
    editor.apply()
  }

  fun saveForceUpdatedAt(_id:String,updateAt:String) {
    val editor = prefs.edit()
    editor.putString(_id, updateAt)
    editor.apply()
  }



  fun getFingerUpdatedAt(_id:String): String? = prefs.getString(_id, null)
  fun getScrollUpdatedAt(_id:String): String? = prefs.getString(_id, null)
  fun getForceUpdatedAt(_id:String): String? = prefs.getString(_id, null)



  //fun getPlayerFingerTime(): String? = prefs.getString(KEY_PLAYER_FINGERPRINT, null)
  //fun getGlobalFingerTime(): String? = prefs.getString(KEY_GLOBAL_FINGERPRINT, null)

  //fun getPlayerForceTime(): String? = prefs.getString(KEY_PLAYER_FORCE, null)
  //fun getGlobalForceTime(): String? = prefs.getString(KEY_GLOBAL_FORCE, null)

}