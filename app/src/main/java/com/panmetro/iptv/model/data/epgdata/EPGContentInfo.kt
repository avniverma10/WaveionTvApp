package com.panmetro.iptv.model.data.epgdata

import androidx.annotation.Keep

@Keep
data class EPGContentInfo(
    val data: List<EPGDataItem>?=null,
    val pagination: Pagination?=null){

    fun isNextPageDataRequired(): Int{
       return (if((pagination?.totalCount?:0) > (pagination?.limit?:0)) {(pagination?.totalCount?:0) - (pagination?.limit?:0)} else 0)
    }

}
@Keep
data class Pagination(
    val totalCount: Int?=null,
    val currentPage: Int?=null,
    val totalPages: Int?=null,
    val offset: Int?=null,
    val limit: Int?=null,
    val hasNextPage: Boolean?=null,
    val hasPrevPage: Boolean?=null,
    val nextOffset: Int?=null,
    val prevOffset: Int?=null,
)
