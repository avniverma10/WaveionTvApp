package com.panmetro.iptv.model.jwt

data class JWTRefreshResponse(
    val session: Session,
    val tokens: Tokens
)


data class Session(
    val device: String,
    val lastActivity: String,
    val sessionId: String
)


data class Tokens(
    val accessExpiresIn: String,
    val accessToken: String,
    val refreshExpiresIn: String,
    val refreshToken: String
)