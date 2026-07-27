package com.crm.enhance_wellness.core.util

import com.crm.enhance_wellness.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray

object BackendImageUrlResolver {
    fun resolve(raw: String?): String? {
        val candidate = firstCandidate(raw) ?: return null
        return resolveCandidate(candidate)
    }

    fun resolve(raw: Any?): String? {
        val candidate = firstCandidate(raw) ?: return null
        return resolveCandidate(candidate)
    }

    fun resolveFirst(vararg rawValues: Any?): String? =
        rawValues.firstNotNullOfOrNull { resolve(it) }

    private fun resolveCandidate(candidate: String): String? {
        val absolute = candidate.toHttpUrlOrNull()
        if (absolute != null && absolute.scheme in setOf("http", "https")) return absolute.toString()

        val base = BuildConfig.BASE_URL.toHttpUrlOrNull() ?: return null
        val origin = base.newBuilder()
            .encodedPath("/")
            .query(null)
            .fragment(null)
            .build()
        return origin.resolve(candidate)?.takeIf { it.scheme in setOf("http", "https") }?.toString()
    }

    private fun firstCandidate(raw: Any?): String? = when (raw) {
        null -> null
        is String -> firstCandidate(raw)
        is Iterable<*> -> raw.firstNotNullOfOrNull { firstCandidate(it) }
        is Array<*> -> raw.firstNotNullOfOrNull { firstCandidate(it) }
        else -> firstCandidate(raw.toString())
    }

    private fun firstCandidate(raw: String?): String? {
        val value = raw?.trim()?.trim('"') ?: return null
        if (value.isInvalidToken()) return null

        if (value.startsWith("[")) {
            val parsed = runCatching { JSONArray(value) }.getOrNull()
            if (parsed != null) {
                for (index in 0 until parsed.length()) {
                    val item = parsed.optString(index).trim()
                    if (!item.isInvalidToken()) return item
                }
                return null
            }
        }

        return value
            .split(',', '|')
            .map { it.trim().trim('"', '\'') }
            .firstOrNull { !it.isInvalidToken() }
    }

    private fun String.isInvalidToken(): Boolean {
        if (isBlank()) return true
        return lowercase() in setOf("null", "undefined", "none", "[]")
    }
}
