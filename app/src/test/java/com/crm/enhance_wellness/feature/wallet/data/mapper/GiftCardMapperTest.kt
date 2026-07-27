package com.crm.enhance_wellness.feature.wallet.data.mapper

import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiftCardMapperTest {

    @Test
    fun `gift card maps optional imageUrl`() {
        val giftCard = GiftCardDto(
            id = 1,
            name = "Wellness Gift",
            amount = 1000,
            price = 900,
            color = null,
            imageUrl = "/uploads/gift.jpg",
            validityDays = 90,
            currency = "INR",
            expiresAt = null,
        ).toDomain()

        assertEquals("https://globuscrm.globussoft.com/uploads/gift.jpg", giftCard.imageUrl)
    }

    @Test
    fun `gift card falls back to thumbnail image field`() {
        val giftCard = GiftCardDto(
            id = 1,
            name = "Wellness Gift",
            amount = 1000,
            price = 900,
            color = null,
            imageUrl = null,
            image = null,
            thumbnailUrl = "https://cdn.example.com/gift-thumb.jpg",
            validityDays = 90,
            currency = "INR",
            expiresAt = null,
        ).toDomain()

        assertEquals("https://cdn.example.com/gift-thumb.jpg", giftCard.imageUrl)
    }
}
