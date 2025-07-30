package com.caastv.tvapp.model.wtvdatabase.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "app_data")
data class AppDataEntity(
    @PrimaryKey
    val id: String,
    val dataType: String,  // "epg", "wtv_manifest", "home_content", "inventory_app"
    val jsonData: String,  // Serialized JSON string
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        // Data type constants
        const val TYPE_EPG_LIST = "wtv_epg_list"
        const val TYPE_WTV_HOME_CATEGORIES = "wtv_home_categories"
        const val TYPE_WTV_MANIFEST = "wtv_manifest"
        const val TYPE_WTV_INVENTORY_APPS = "wtv_inventory_apps"
        const val TYPE_WTV_BANNERS = "wtv_banners"

        // ID constants for singleton records
        const val ID_ALL_EPG = "all_epg_data"
        const val ID_HOME_CATEGORIES = "home_categories"
        const val ID_INVENTORY_APPS = "inventory_apps"
        const val ID_BANNERS = "banners"
        const val ID_MANIFEST = "manifest"
    }


}