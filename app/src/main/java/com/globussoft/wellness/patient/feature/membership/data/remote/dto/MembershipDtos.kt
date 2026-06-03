package com.globussoft.wellness.patient.feature.membership.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembershipDto(
    val id: Int,
    val status: String,
    val startDate: String,
    val endDate: String,
    val daysLeft: Int,
    val plan: MembershipPlanRefDto,
    val credits: List<MembershipCreditDto>,
    val history: List<MembershipHistoryDto>,
)

@JsonClass(generateAdapter = true)
data class MembershipPlanRefDto(
    val id: Int,
    val name: String,
    val price: Double,
    val currency: String,
)

@JsonClass(generateAdapter = true)
data class MembershipCreditDto(
    val serviceId: Int,
    val serviceName: String,
    val totalCredits: Int,
    val usedCredits: Int,
    val remainingCredits: Int,
)

@JsonClass(generateAdapter = true)
data class MembershipHistoryDto(
    val date: String,
    val serviceName: String,
    val creditsUsed: Int,
)

@JsonClass(generateAdapter = true)
data class MembershipPlanDto(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val services: List<MembershipPlanServiceDto>,
)

@JsonClass(generateAdapter = true)
data class MembershipPlanServiceDto(
    val serviceId: Int,
    val serviceName: String,
    val credits: Int,
)
