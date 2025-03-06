package com.example.tvapp.models

import com.example.applicationscreens.models.Data

data class ContentResponse(
    val `data`: List<Data>,
    val success: Boolean
)