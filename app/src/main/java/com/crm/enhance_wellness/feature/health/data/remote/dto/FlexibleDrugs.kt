package com.crm.enhance_wellness.feature.health.data.remote.dto

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Qualifier for the prescription `drugs` field. Some backends send it as a JSON-encoded
 * string ("[{...}]"), others as a real JSON array ([{...}]). This adapter normalises both
 * to a JSON string so the existing mapper can parse it consistently.
 */
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class FlexibleDrugs

class FlexibleDrugsAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return if (annotations.any { it is FlexibleDrugs }) {
            FlexibleDrugsAdapter(moshi)
        } else {
            null
        }
    }
}

private class FlexibleDrugsAdapter(private val moshi: Moshi) : JsonAdapter<String>() {
    override fun fromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.BEGIN_ARRAY,
            JsonReader.Token.BEGIN_OBJECT -> {
                val raw = reader.readJsonValue()
                if (raw == null) null else moshi.adapter(Any::class.java).toJson(raw)
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: String?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value)
        }
    }
}
