package com.panmetro.iptv.model.data

import com.panmetro.iptv.model.data.epgdata.EPGDataItem

data class ServerEPGState(
        val isServerAvailable: Boolean,
        val epgList: List<EPGDataItem>
    )