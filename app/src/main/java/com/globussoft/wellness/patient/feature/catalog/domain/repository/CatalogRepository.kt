package com.globussoft.wellness.patient.feature.catalog.domain.repository

import com.globussoft.wellness.patient.feature.catalog.domain.model.Service
import com.globussoft.wellness.patient.feature.catalog.domain.model.ServiceCategory

interface CatalogRepository {
    suspend fun getServices(): List<Service>
    suspend fun getCategories(): List<ServiceCategory>
}
