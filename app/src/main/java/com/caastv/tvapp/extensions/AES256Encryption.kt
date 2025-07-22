package com.caastv.tvapp.extensions

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES256Encryption {

    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val AES = "AES"
    private const val FIXED_IV = "1234567890123456" // 16-byte IV — can be randomized if needed

    private fun getSecretKey(key: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(key.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, AES)
    }

    fun encrypt(data: ByteArray, key: String): ByteArray {
        val secretKey = getSecretKey(key)
        val iv = IvParameterSpec(FIXED_IV.toByteArray(Charsets.UTF_8))
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray, key: String): ByteArray {
        val secretKey = getSecretKey(key)
        val iv = IvParameterSpec(FIXED_IV.toByteArray(Charsets.UTF_8))
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher.doFinal(data)
    }
}
