package com.panmetro.iptv.model.data.appupdate

import androidx.annotation.Keep
import kotlin.text.equals

@Keep
data class AppUpdateData(
    val _id: String,
    val appVersion: String,
    val apkUrl: String,
    val updateDate: String,
    val forceUpdate: Int,
    val regions: ArrayList<RegionInfo>?=null
){
    fun checkRegionForceUpdate(regionCode:String?): Boolean{
        val allRegion = regions?.getOrNull(0)
        val matchingRegion = regions?.firstOrNull {
            it.code.equals(regionCode, ignoreCase = true)
        }
        if(allRegion?.code?.equals("all",true) == true && allRegion.forceUpdate == true){
            return true
        } else if(matchingRegion?.forceUpdate == true){
            return true
        }else {
            return false
        }
    }

    fun checkRegionUpdate(regionCode:String?): Boolean{
        val allRegion = regions?.getOrNull(0)
        val matchingRegion = regions?.firstOrNull {
            it.code.equals(regionCode, ignoreCase = true)
        }
        if(allRegion?.code?.equals("all",true) == true){
            return true
        } else if(matchingRegion != null){
            return true
        }else {
            return false
        }
    }
}

data class RegionInfo(val code: String?=null,val name: String?=null,val forceUpdate: Boolean?=false,val _id: String?=null)