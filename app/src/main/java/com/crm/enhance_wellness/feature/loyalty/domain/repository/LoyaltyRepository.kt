package com.crm.enhance_wellness.feature.loyalty.domain.repository

import com.crm.enhance_wellness.feature.loyalty.domain.model.LoyaltyData

interface LoyaltyRepository {
    suspend fun getLoyalty(): LoyaltyData
}
