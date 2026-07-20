package com.crm.enhance_wellness.feature.catalog.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.feature.catalog.data.mapper.toDomain
import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory
import com.crm.enhance_wellness.feature.catalog.domain.repository.CatalogRepository
import retrofit2.HttpException
import javax.inject.Inject

class CatalogRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
) : CatalogRepository {

    override suspend fun getServices(): List<Service> {
        val response = api.getCatalogServices()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body().orEmpty().map { it.toDomain() }
    }

    override suspend fun getCategories(): List<ServiceCategory> {
        val response = api.getCatalogServiceCategories()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body().orEmpty().map { it.toDomain() }
    }
}
