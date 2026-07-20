package com.crm.enhance_wellness.feature.loyalty.data.mapper

import com.crm.enhance_wellness.feature.loyalty.data.remote.dto.LoyaltyResponseDto
import com.crm.enhance_wellness.feature.loyalty.data.remote.dto.LoyaltyTransactionDto
import com.crm.enhance_wellness.feature.loyalty.domain.model.LoyaltyData
import com.crm.enhance_wellness.feature.loyalty.domain.model.LoyaltyTransaction

fun LoyaltyResponseDto.toDomain() = LoyaltyData(
    balance = balance,
    earnedThisMonth = earnedThisMonth,
    transactions = transactions.map { it.toDomain() },
)

fun LoyaltyTransactionDto.toDomain() = LoyaltyTransaction(
    id = id,
    type = type,
    points = points,
    reason = reason,
    createdAt = createdAt,
)
