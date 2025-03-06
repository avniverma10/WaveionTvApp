package com.example.tvapp.utils
import android.util.Base64
import org.json.JSONObject
import android.util.Log

object JwtUtils {
    fun decodeJwtToken(token: String?): String? {
        return try {
            if (token == null) return null
            val parts = token.split(".") // JWT consists of header, payload, signature
            if (parts.size < 2) return null // Ensure it's a valid token

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE)) // Decode payload
            val jsonObject = JSONObject(payload)

            // Log full JWT payload for debugging
            Log.d("HASH", "Decoded Payload: $payload")


            // Extract phone number (fallback to `sub` if no phone field exists)
            jsonObject.optString("phone", jsonObject.optString("sub", null)) //  Now extracts `sub`

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
