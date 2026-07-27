package com.crm.enhance_wellness.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BackendImageUrlResolverTest {

    @Test
    fun `absolute http urls remain unchanged`() {
        assertEquals(
            "https://cdn.example.com/image.jpg",
            BackendImageUrlResolver.resolve("https://cdn.example.com/image.jpg"),
        )
    }

    @Test
    fun `relative paths resolve against backend origin`() {
        assertEquals(
            "https://globuscrm.globussoft.com/uploads/image.jpg",
            BackendImageUrlResolver.resolve("/uploads/image.jpg"),
        )
    }

    @Test
    fun `json array strings use first valid url`() {
        assertEquals(
            "https://cdn.example.com/one.jpg",
            BackendImageUrlResolver.resolve("""["", "https://cdn.example.com/one.jpg"]"""),
        )
    }

    @Test
    fun `actual lists use first valid url`() {
        assertEquals(
            "https://globuscrm.globussoft.com/uploads/one.jpg",
            BackendImageUrlResolver.resolve(listOf("", "null", "/uploads/one.jpg")),
        )
    }

    @Test
    fun `resolveFirst uses first usable aliased value`() {
        assertEquals(
            "https://cdn.example.com/thumb.jpg",
            BackendImageUrlResolver.resolveFirst(null, "undefined", listOf("", "https://cdn.example.com/thumb.jpg")),
        )
    }

    @Test
    fun `delimited strings use first valid url`() {
        assertEquals(
            "https://cdn.example.com/one.jpg",
            BackendImageUrlResolver.resolve("null, https://cdn.example.com/one.jpg"),
        )
    }

    @Test
    fun `invalid tokens return null`() {
        assertNull(BackendImageUrlResolver.resolve(""))
        assertNull(BackendImageUrlResolver.resolve("null"))
        assertNull(BackendImageUrlResolver.resolve("undefined"))
        assertNull(BackendImageUrlResolver.resolve("ftp://cdn.example.com/image.jpg"))
    }
}
