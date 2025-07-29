package com.caastv.tvapp.model.data.favourites

data class FavouritesResponse(
    val message: String,
    val data: FavouritesData
)

data class FavouritesData(
    val userID: String,
    val channelId: List<String>,
    val _id: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val __v: Int? = null
)