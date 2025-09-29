package com.panmetro.iptv.model.data.login

import com.google.gson.annotations.SerializedName

data class CustomerChannelsInfo(
    val count: Int,
    val page: Int,
    @SerializedName("results-per-page")
    val resultsPerPage: Int,
    val results: List<ChannelResult>
)


data class ChannelResult(
    @SerializedName("asset-id")
    val assetId: Int,
    @SerializedName("content-id")
    val contentId: String,
    val name: String
)