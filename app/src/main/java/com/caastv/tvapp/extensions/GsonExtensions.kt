package com.caastv.tvapp.extensions

import com.caastv.tvapp.utils.CoreJsonString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


fun Any.provideGsonWithCoreJsonString(): Gson {
    return GsonBuilder().also {
        it.registerTypeAdapter(
            CoreJsonString::class.java,
            object : JsonDeserializer<CoreJsonString?> {
                override fun deserialize(
                    json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
                ): CoreJsonString? {
                    return json?.toString()?.let { time ->
                        CoreJsonString(time)
                    } ?: kotlin.run { null }
                }

            })
        it.registerTypeAdapter(
            CoreJsonString::class.java,
            object : JsonSerializer<CoreJsonString?> {
                override fun serialize(
                    src: CoreJsonString?, typeOfSrc: Type?, context: JsonSerializationContext?
                ): JsonElement {
                    val parser = JsonParser()
                    return parser.parse(src?.data)
                }
            })
    }.create()
}
