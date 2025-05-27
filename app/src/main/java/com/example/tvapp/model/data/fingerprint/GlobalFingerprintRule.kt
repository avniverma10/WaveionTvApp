package com.example.tvapp.model.data.fingerprint

import com.google.gson.annotations.SerializedName

data class GlobalFingerprintRule(
    @SerializedName("__v")
    val version: Int??=null,
    val _id: String??=null,
    val backgroundColorHex: String?="#000000",
    val backgroundTransparency:  String?=".5",
    val createdAt: String?=null,
    val durationMs: Int?=60000,
    val enabled: Boolean??=null,
    val fingerprintName: String??=null,
    val fingerprintScope: String??=null,
    val fingerprintType: String?="COVERT", // "COVERT" or "OVERT",
    val fontColorHex: String?="#ffffff",
    val fontFamily: String??=null,
    val fontSizeDp: Int?=12,
    val fontTransparency: String?=".5",
    val id: String??=null,
    val intervalSec: Int?=5,
    val method: String?="BASE16",
    val obfuscationKey: String?="12",
    val posXPercent: Float?=null,
    val posYPercent: Float?=null,
    val positionMode: String?="RANDOM", // "FIXED" or "RANDOM",
    val repeatCount: Int?=5,
    val textSeed: String?="mac id",
    val updatedAt: String??=null,
    val userFilter: List<String>?=null
)
