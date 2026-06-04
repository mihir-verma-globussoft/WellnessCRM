package com.globussoft.wellness.patient.feature.loyalty.domain.repository

import com.globussoft.wellness.patient.feature.loyalty.domain.model.LoyaltyData

interface LoyaltyRepository {
    suspend fun getLoyalty(): LoyaltyData
}
