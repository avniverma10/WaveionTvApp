package com.caastv.tvapp.extensions


import android.content.Context
import android.util.Base64
import com.caastv.tvapp.utils.fingerprint.Base32Encoder
import com.caastv.tvapp.utils.fingerprint.Base58
import com.caastv.tvapp.utils.fingerprint.decrypt
import com.caastv.tvapp.utils.fingerprint.encrypt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.generateTextFingerprint(method:String?="plain text", obfuscationKey:String): String {
    val macAddr = provideMacAddress() ?: "00:00:00:00:00:00"
    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
    var input = macAddr
   // var input = "$macAddr|$timestamp"

    /*timestamp?.takeIf { it.isNotBlank() }?.let {
        input += "$macAddr|$timestamp"
    }*/

    obfuscationKey?.takeIf { it.isNotBlank() }?.let {
        input += "|$it"
    }

    val inputBytes = input.toByteArray()


    return when (method?.uppercase(Locale.US)) {
        "BASE16" -> inputBytes.joinToString("") { "%02X".format(it) }//AES256Encryption.encrypt(inputBytes, obfuscationKey).joinToString("") { "%02X".format(it) }

        "BASE32" -> Base32Encoder.encode(inputBytes)//AES256Encryption.encrypt(inputBytes, obfuscationKey))

        "BASE58" -> Base58.encode(inputBytes)//Base58.encode(AES256Encryption.encrypt(inputBytes, obfuscationKey))

        "BASE64" -> Base64.encodeToString(inputBytes,Base64.NO_WRAP)//AES256Encryption.encrypt(inputBytes, obfuscationKey),Base64.NO_WRAP)

        "PLAIN TEXT", null -> input

        else -> "UNKNOWN_METHOD"
    }
}

fun generateShortDisplayFingerprint(input: String, obfuscationKey: String): String {
    val encrypted = AES256Encryption.encrypt(input.toByteArray(Charsets.UTF_8), obfuscationKey)
    return Base58.encode(encrypted)
}

fun decodeDisplayFingerprint(encoded: String, obfuscationKey: String): String {
    val decodedBytes = Base58.decode(encoded)
    val decryptedBytes = AES256Encryption.decrypt(decodedBytes, obfuscationKey)
    return String(decryptedBytes, Charsets.UTF_8)
}

fun generateDisplayFingerprint(input: String,key:String): String {
    return Base64.encodeToString(encrypt(input.toByteArray(), key), Base64.NO_WRAP)
}

fun recoverDisplayFingerprint(encoded: String, key: String): String {
    val bytes = Base58.decode(encoded)
    return String(decrypt(bytes, key))
}