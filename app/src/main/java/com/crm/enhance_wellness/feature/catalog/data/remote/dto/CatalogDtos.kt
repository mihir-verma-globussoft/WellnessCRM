package com.crm.enhance_wellness.feature.catalog.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogServiceDto(
    val id: Int,
    val name: String,
    val description: String?,
    val basePrice: Double?,
    val discountedPrice: Double?,
    val imageUrls: Any?,
    val imageUrl: Any? = null,
    val image: Any? = null,
    val thumbnailUrl: Any? = null,
    val thumbnail: Any? = null,
    val photoUrl: Any? = null,
    val pictureUrl: Any? = null,
    val category: String?,
    val categoryId: Int?,
    val durationMin: Int?,
    val isActive: Boolean?,
)

@JsonClass(generateAdapter = true)
data class CatalogServiceCategoryDto(
    val id: Int,
    val name: String,
    val parentId: Int?,
    val imageUrl: Any?,
    val image: Any? = null,
    val thumbnailUrl: Any? = null,
    val thumbnail: Any? = null,
    val iconUrl: Any? = null,
    val color: String?,
    @Json(name = "_count") val count: CategoryCountDto?,
)

@JsonClass(generateAdapter = true)
data class CategoryCountDto(
    val services: Int?,
)

// GET /api/packages — absolute path (not under /api/wellness/), CUSTOMER JWT.
// Reverse-engineered from the patient web portal's Catalog > Packages tab (2026-08-31);
// only the read path is confirmed — purchase (Razorpay order/confirm) is not wired yet.
@JsonClass(generateAdapter = true)
data class ServicePackagesResponseDto(
    val packages: List<ServicePackageDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ServicePackageDto(
    val id: Int,
    val name: String,
    val description: String?,
    val sessions: Int,
    val discountPercent: Int?,
    val grossPrice: Double?,
    val price: Double,
    val currency: String,
    val taxPercent: Int?,
    val validityDays: Int?,
    val sellByDate: String?,
    val isActive: Boolean,
    val services: List<PackageServiceDto> = emptyList(),
    val ownedPlan: OwnedServicePlanDto? = null,
)

@JsonClass(generateAdapter = true)
data class PackageServiceDto(
    val id: Int,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class OwnedServicePlanDto(
    val status: String,
    val totalSessions: Int,
    val completedSessions: Int,
)
