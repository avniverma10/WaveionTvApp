package com.android.panmetroiptv.view.epg

import com.android.panmetroiptv.model.data.epgdata.EPGDataItem

data class EPGUIState(
    val loading: Boolean? = false,
    val epgDataItemList: List<EPGDataItem>? = emptyList(),
    val selectedEPGDataItem: EPGDataItem? = null,
    val selectedCategory: String? = "",
    val errorMessage: String? = null
)
