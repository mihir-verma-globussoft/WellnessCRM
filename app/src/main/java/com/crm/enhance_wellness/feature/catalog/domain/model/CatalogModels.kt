package com.crm.enhance_wellness.feature.catalog.domain.model

data class Service(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double?,
    val discountedPrice: Double?,
    val imageUrl: String?,
    val categoryName: String?,
    val duration: Int?,    // minutes, may be null
    val isActive: Boolean,
)

data class ServiceCategory(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val imageUrl: String?,
    val color: String?,
    val servicesCount: Int,
)

data class ServicePackage(
    val id: Int,
    val name: String,
    val description: String?,
    val sessions: Int,
    val discountPercent: Int,
    val grossPrice: Double?,
    val price: Double,
    val currency: String,
    val taxPercent: Int,
    val validityDays: Int?,
    val sellByDate: String?,
    val isActive: Boolean,
    val serviceNames: List<String>,
    val ownedPlan: OwnedServicePlan?,
)

data class OwnedServicePlan(
    val status: String,
    val totalSessions: Int,
    val completedSessions: Int,
)
