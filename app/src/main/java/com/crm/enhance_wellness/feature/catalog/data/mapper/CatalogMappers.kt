package com.crm.enhance_wellness.feature.catalog.data.mapper

import com.crm.enhance_wellness.core.util.BackendImageUrlResolver
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceCategoryDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.OwnedServicePlanDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.ServicePackageDto
import com.crm.enhance_wellness.feature.catalog.domain.model.OwnedServicePlan
import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory
import com.crm.enhance_wellness.feature.catalog.domain.model.ServicePackage

fun CatalogServiceDto.toDomain() = Service(
    id = id,
    name = name,
    description = description,
    price = basePrice,
    discountedPrice = discountedPrice,
    imageUrl = BackendImageUrlResolver.resolveFirst(
        imageUrl,
        imageUrls,
        image,
        thumbnailUrl,
        thumbnail,
        photoUrl,
        pictureUrl,
    ),
    categoryName = category,
    duration = durationMin,
    isActive = isActive ?: true,
)

fun CatalogServiceCategoryDto.toDomain() = ServiceCategory(
    id = id,
    name = name,
    parentId = parentId,
    imageUrl = BackendImageUrlResolver.resolveFirst(imageUrl, image, thumbnailUrl, thumbnail, iconUrl),
    color = color,
    servicesCount = count?.services ?: 0,
)

fun ServicePackageDto.toDomain() = ServicePackage(
    id = id,
    name = name,
    description = description,
    sessions = sessions,
    discountPercent = discountPercent ?: 0,
    grossPrice = grossPrice,
    price = price,
    currency = currency,
    taxPercent = taxPercent ?: 0,
    validityDays = validityDays,
    sellByDate = sellByDate,
    isActive = isActive,
    serviceNames = services.map { it.name },
    ownedPlan = ownedPlan?.toDomain(),
)

fun OwnedServicePlanDto.toDomain() = OwnedServicePlan(
    status = status,
    totalSessions = totalSessions,
    completedSessions = completedSessions,
)
