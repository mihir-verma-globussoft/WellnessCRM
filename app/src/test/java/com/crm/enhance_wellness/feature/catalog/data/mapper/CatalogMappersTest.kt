package com.crm.enhance_wellness.feature.catalog.data.mapper

import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogMappersTest {

    @Test
    fun `service imageUrls maps single url to imageUrl`() {
        val service = CatalogServiceDto(
            id = 1,
            name = "Facial",
            description = null,
            basePrice = 500.0,
            discountedPrice = null,
            imageUrls = "https://cdn.example.com/facial.jpg",
            category = "Skin",
            categoryId = 10,
            durationMin = 30,
            isActive = true,
        ).toDomain()

        assertEquals("https://cdn.example.com/facial.jpg", service.imageUrl)
    }

    @Test
    fun `service imageUrls maps json array to first usable url`() {
        val service = CatalogServiceDto(
            id = 1,
            name = "Facial",
            description = null,
            basePrice = 500.0,
            discountedPrice = null,
            imageUrls = """["", "/uploads/facial.jpg"]""",
            category = "Skin",
            categoryId = 10,
            durationMin = 30,
            isActive = true,
        ).toDomain()

        assertEquals("https://globuscrm.globussoft.com/uploads/facial.jpg", service.imageUrl)
    }
}
