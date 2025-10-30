package com.panmetro.iptv.model.jwt

data class RefreshBody(
    val username: String,
    val password: String,
    val refreshToken: String,
   // val sessionToken: String
)