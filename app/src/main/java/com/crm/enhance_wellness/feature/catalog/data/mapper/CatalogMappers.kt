package com.crm.enhance_wellness.feature.catalog.data.mapper

import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceCategoryDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceDto
import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory

fun CatalogServiceDto.toDomain() = Service(
    id = id,
    name = name,
    description = description,
    price = basePrice,
    discountedPrice = discountedPrice,
    imageUrl = imageUrls,
    categoryName = category,
    duration = durationMin,
    isActive = isActive ?: true,
)

fun CatalogServiceCategoryDto.toDomain() = ServiceCategory(
    id = id,
    name = name,
    parentId = parentId,
    imageUrl = imageUrl,
    color = color,
    servicesCount = count?.services ?: 0,
)
