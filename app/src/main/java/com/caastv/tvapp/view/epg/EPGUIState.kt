package com.caastv.tvapp.view.epg

import com.caastv.tvapp.model.data.epgdata.EPGDataItem

data class EPGUIState(
    val loading: Boolean? = false,
    val epgDataItemList: List<EPGDataItem>? = emptyList(),
    val selectedEPGDataItem: EPGDataItem? = null,
    val selectedCategory: String? = "",
    val errorMessage: String? = null
)
