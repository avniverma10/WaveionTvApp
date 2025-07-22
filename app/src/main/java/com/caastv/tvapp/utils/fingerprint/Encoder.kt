package com.caastv.tvapp.utils.fingerprint

import java.math.BigInteger
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val FIXED_IV = "1234567890123456" // 16-byte IV — can be randomized if needed


object Base32Encoder {
    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val BASE32_LOOKUP = IntArray(256) { -1 }

    init {
        BASE32_CHARS.forEachIndexed { index, c ->
            BASE32_LOOKUP[c.code] = index
            BASE32_LOOKUP[c.lowercaseChar().code] = index // Allow lowercase input
        }
    }

    fun encode(data: ByteArray): String {
        val output = StringBuilder()
        var buffer = 0
        var bitsLeft = 0

        for (byte in data) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bitsLeft += 8

            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1F
                output.append(BASE32_CHARS[index])
                bitsLeft -= 5
            }
        }

        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1F
            output.append(BASE32_CHARS[index])
        }

        // Optional padding with '=' to make length a multiple of 8
        while (output.length % 8 != 0) {
            output.append('=')
        }

        return output.toString()
    }

    fun decode(input: String): ByteArray {
        val cleanInput = input.trimEnd('=').uppercase(Locale.US)
        val output = mutableListOf<Byte>()

        var buffer = 0
        var bitsLeft = 0

        for (char in cleanInput) {
            val value = BASE32_LOOKUP[char.code]
            if (value == -1) throw IllegalArgumentException("Invalid Base32 character: $char")

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                output.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }

        return output.toByteArray()
    }
}

object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(58)

    private val CHAR_TO_INDEX = ALPHABET.withIndex().associate { it.value to it.index }
    fun encode(input: ByteArray): String {
        var num = BigInteger(1, input)
        val result = StringBuilder()

        while (num > BigInteger.ZERO) {
            val remainder = num % BASE
            result.insert(0, ALPHABET[remainder.toInt()])
            num /= BASE
        }

        // Add '1' for each leading 0 byte
        for (byte in input) {
            if (byte.toInt() == 0) {
                result.insert(0, '1')
            } else break
        }

        return result.toString()
    }

    fun decode(input: String): ByteArray {
        var num = BigInteger.ZERO

        for (char in input) {
            val index = CHAR_TO_INDEX[char]
                ?: throw IllegalArgumentException("Invalid Base58 character: $char")
            num = num * BASE + BigInteger.valueOf(index.toLong())
        }

        // Add leading zeros
        val byteList = num.toByteArray().dropWhile { it == 0.toByte() }.toMutableList()
        for (char in input) {
            if (char == '1') byteList.add(0, 0)
            else break
        }

        return byteList.toByteArray()
    }
}



fun encrypt(data: ByteArray, key: String): ByteArray {
    val iv = IvParameterSpec(FIXED_IV.toByteArray(Charsets.UTF_8))
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key), iv)
    return cipher.doFinal(data)
}
fun decrypt(data: ByteArray, key: String): ByteArray {
    val iv = IvParameterSpec(FIXED_IV.toByteArray(Charsets.UTF_8))
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), iv)
    return cipher.doFinal(data)
}


private fun getSecretKey(key: String): SecretKeySpec {
    val sha = MessageDigest.getInstance("SHA-256")
    val keyBytes = sha.digest(key.toByteArray(Charsets.UTF_8))
    return SecretKeySpec(keyBytes, "AES")
}
