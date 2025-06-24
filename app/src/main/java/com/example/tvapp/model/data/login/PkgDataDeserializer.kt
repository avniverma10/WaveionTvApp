// util/PkgDataDeserializer.kt
package com.example.tvapp.model.data.login

import androidx.annotation.Keep
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

@Keep
class PkgDataDeserializer : JsonDeserializer<Pkgdata> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Pkgdata {
        return when {
            // Case 1: JSON sent as an array (e.g. "pkgdata": [])
            json.isJsonArray -> {
                // If it’s simply an empty array (or even a non-empty array), 
                // we’ll treat it as “no pkg data.”
                // You could inspect the array’s contents here if your API sometimes returns 
                // actual objects inside that array; but in your examples it’s always empty.
                Pkgdata()
            }

            // Case 2: JSON sent as an object (e.g. "pkgdata": { "activepackcount": 1, "activepack":[ … ] })
            json.isJsonObject -> {
                val obj = json.asJsonObject

                // Read "activepackcount" (fallback to 0 if missing or not an int)
                val countElement = obj.get("activepackcount")
                val activePackCount = if (countElement != null && countElement.isJsonPrimitive && countElement.asJsonPrimitive.isNumber) {
                    countElement.asInt
                } else {
                    0
                }

                // Read "activepack" array (if missing or malformed, default to emptyList())
                val activePackList: List<Activepack> = if (obj.has("activepack") && obj.get("activepack").isJsonArray) {
                    val array = obj.getAsJsonArray("activepack")
                    array.mapNotNull { element ->
                        try {
                            context.deserialize<Activepack>(element, Activepack::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                } else {
                    emptyList()
                }

                Pkgdata(
                    activepackcount = activePackCount,
                    activepack = activePackList
                )
            }

            // Any other form: treat as “no pkg data.”
            else -> Pkgdata()
        }
    }
}
