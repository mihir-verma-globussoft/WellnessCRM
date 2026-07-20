package com.crm.enhance_wellness.feature.catalog.domain.repository

import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory

interface CatalogRepository {
    suspend fun getServices(): List<Service>
    suspend fun getCategories(): List<ServiceCategory>
}
