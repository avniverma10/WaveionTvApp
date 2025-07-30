package com.caastv.tvapp.model.repository.database

import android.util.Log
import com.caastv.tvapp.extensions.convertDatabaseModels
import com.caastv.tvapp.extensions.convertIntoModels
import com.caastv.tvapp.model.data.banner.Banner
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.manifest.WTVManifest
import com.caastv.tvapp.model.home.WTVHomeCategory
import com.caastv.tvapp.model.wtvdatabase.database.dao.AppDataDao
import com.caastv.tvapp.model.wtvdatabase.database.entity.AppDataEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepository @Inject constructor(
    private val dao: AppDataDao,
    private val gson: Gson
) {

    // EPG Data List operations
    suspend fun saveAllEpgData(epgList: String) {
        dao.replaceAllEpgData(epgList)
    }

    suspend fun getAllEpgData(): List<EPGDataItem> {
        return try {
            val entity = dao.getByIdAndType(
                AppDataEntity.ID_ALL_EPG,
                AppDataEntity.TYPE_EPG_LIST
            ) ?: return emptyList()
            Log.d("EPG_DEBUG", "Raw JSON: ${entity.jsonData}")
          val epgData =  entity.jsonData.convertIntoModels(object : TypeToken<List<EPGDataItem>>() {})?:emptyList()
           epgData
        } catch (e: Exception) {
            Log.e("EPG_PARSE_ERROR", "Failed to parse EPG data", e)
            emptyList()
        }
    }

    // Home Categories operations
    suspend fun saveAllHomeCategories(categories: List<WTVHomeCategory>) {
        dao.replaceAllHomeCategories(categories)
    }
    suspend fun getAllHomeCategories(): List<WTVHomeCategory> {
        val entity = dao.getByIdAndType(
            AppDataEntity.ID_HOME_CATEGORIES,
            AppDataEntity.TYPE_WTV_HOME_CATEGORIES
        )
        return entity?.let {
            it.jsonData.convertIntoModels(object : TypeToken<List<WTVHomeCategory>>() {}) ?: emptyList()
        } ?: emptyList()
    }


    // Apps Inventory operations
    suspend fun saveAllAppsInventory(categories: List<InventoryApp>) {
        dao.replaceAllInventoryApps(categories)
    }


    suspend fun getAllAppsInventory(): List<InventoryApp> {
        val entity = dao.getByIdAndType(
            AppDataEntity.ID_INVENTORY_APPS,
            AppDataEntity.TYPE_WTV_INVENTORY_APPS
        )
        return entity?.let { it.jsonData.convertIntoModels(object : TypeToken<List<InventoryApp>>() {}) ?: emptyList()
        } ?: emptyList()
    }

    // Banner operations
    suspend fun saveAllBanner(banners: List<Banner>) {
        dao.replaceAllBanners(banners)
    }


    suspend fun getAllBanner(): List<Banner> {
        val entity = dao.getByIdAndType(
            AppDataEntity.ID_BANNERS,
            AppDataEntity.TYPE_WTV_BANNERS
        )
        return entity?.let { it.jsonData.convertIntoModels(object : TypeToken<List<Banner>>() {}) ?: emptyList()
        } ?: emptyList()
    }


    // WTV Manifest operations
    suspend fun saveWtvManifest(manifest: WTVManifest) {
        dao.insertOrUpdate(
            AppDataEntity(
                id = AppDataEntity.ID_MANIFEST,
                dataType = AppDataEntity.TYPE_WTV_MANIFEST,
                jsonData = gson.toJson(manifest)
            )
        )
    }

    suspend fun getWtvManifest(): WTVManifest? {
        return dao.getByIdAndType(
            AppDataEntity.ID_MANIFEST,
            AppDataEntity.TYPE_WTV_MANIFEST
        )?.let {
            gson.fromJson(it.jsonData, WTVManifest::class.java)
        }
    }

    // Clear operations
    suspend fun clearAllEpgData() = dao.deleteByType(AppDataEntity.TYPE_EPG_LIST)
    suspend fun clearAllHomeCategories() = dao.deleteByType(AppDataEntity.TYPE_WTV_HOME_CATEGORIES)
}