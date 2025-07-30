package com.caastv.tvapp.model.wtvdatabase.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.caastv.tvapp.model.data.banner.Banner
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.home.WTVHomeCategory
import com.caastv.tvapp.model.wtvdatabase.database.entity.AppDataEntity
import com.google.gson.Gson

@Dao
interface AppDataDao {
    // Base operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(data: AppDataEntity)

    @Query("SELECT * FROM app_data WHERE id = :id AND dataType = :dataType LIMIT 1")
    suspend fun getByIdAndType(id: String, dataType: String): AppDataEntity?

    @Query("DELETE FROM app_data WHERE dataType = :dataType")
    suspend fun deleteByType(dataType: String)

    // Specialized list operations
    @Transaction
    suspend fun replaceAllEpgData(epgList: String) {
        deleteByType(AppDataEntity.TYPE_EPG_LIST)
        insertOrUpdate(
            AppDataEntity(
                id = AppDataEntity.ID_ALL_EPG,
                dataType = AppDataEntity.TYPE_EPG_LIST,
                jsonData = epgList
            )
        )
    }

    @Transaction
    suspend fun replaceAllHomeCategories(categories: List<WTVHomeCategory>) {
        deleteByType(AppDataEntity.TYPE_WTV_HOME_CATEGORIES)
        insertOrUpdate(
            AppDataEntity(
                id = AppDataEntity.ID_HOME_CATEGORIES,
                dataType = AppDataEntity.TYPE_WTV_HOME_CATEGORIES,
                jsonData = Gson().toJson(categories)
            )
        )
    }
    @Transaction
    suspend fun replaceAllInventoryApps(categories: List<InventoryApp>) {
        deleteByType(AppDataEntity.TYPE_WTV_INVENTORY_APPS)
        insertOrUpdate(
            AppDataEntity(
                id = AppDataEntity.ID_INVENTORY_APPS,
                dataType = AppDataEntity.TYPE_WTV_INVENTORY_APPS,
                jsonData = Gson().toJson(categories)
            )
        )
    }

    @Transaction
    suspend fun replaceAllBanners(banners: List<Banner>) {
        deleteByType(AppDataEntity.TYPE_WTV_BANNERS)
        insertOrUpdate(
            AppDataEntity(
                id = AppDataEntity.ID_BANNERS,
                dataType = AppDataEntity.TYPE_WTV_BANNERS,
                jsonData = Gson().toJson(banners)
            )
        )
    }
}