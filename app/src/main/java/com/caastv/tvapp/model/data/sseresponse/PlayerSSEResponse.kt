package com.caastv.tvapp.model.data.sseresponse

import com.google.gson.annotations.SerializedName

data class PlayerSSEResponse(
    val fingerprints: List<PlayerFingerprint>,
    val scrollMessages: List<ScrollMessage>,
    val forceMessages: List<ForceMessage>
)

data class PlayerFingerprint(
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

data class PlayerScrollMessage(
        @SerializedName("__v")
        val version: Int?=null,
        val _id: String?=null,
        val backgroundColorHex: String?="#ffffff",
        val backgroundTransparency: String?=".5",
        val createdAt: String?=null,
        val enabled: Boolean?=null,
        val fontColorHex: String?="#000000",
        val fontFamily: String?=null,
        val fontSizeDp: Int?=12,//min 12 and max 50-60
        val fontTransparency: String?=".5",
        val id: String?=null,
        val message: String?=null,
        val messageName: String?=null,
        val posXPercent: Float?=null,
        val posYPercent: Float?=null,
        val positionMode: String?="RANDOM", // "FIXED" or "RANDOM",
        val repeatCount: Int?=5,
        val randomIntervalSec: Int?=10,//for random case handling required it default is 10
        val scrollSpeed: Float?=null,
        val updatedAt: String?=null,
        val messageScope: String?="GLOBAL"
)