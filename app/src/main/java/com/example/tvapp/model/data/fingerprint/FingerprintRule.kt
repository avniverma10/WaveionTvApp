package com.example.tvapp.model.data.fingerprint


data class FingerprintRule(
    val id: String?="",
    val fingerprintType: String?="COVERT", // "COVERT" or "OVERT"
    val textSeed: String?="mac id",
    val fontFamily: String?="",
    val fontSizeDp: Int?=12,
    val fontColorHex: String?="#8a0e0e",//"#f2e8e8",
    val fontTransparency: String?="0.65",
    val backgroundColorHex: String?="#0be6cc",//"#0d0c0c",
    val backgroundTransparency: String?="0.65",
    val positionMode: String?="RANDOM", // "FIXED" or "RANDOM"
    val posXPercent: Float?=null,
    val posYPercent: Float?=null,
    val intervalSec: Int?=10,
    val durationMs: Int?=60000,
    val repeatCount: Int?=5,
    val obfuscationKey: String?="12",
    val method: String?="BASE64"
)