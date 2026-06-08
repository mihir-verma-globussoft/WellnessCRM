package com.globussoft.wellness.patient.feature.catalog.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.feature.catalog.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.catalog.domain.model.Service
import com.globussoft.wellness.patient.feature.catalog.domain.model.ServiceCategory
import com.globussoft.wellness.patient.feature.catalog.domain.repository.CatalogRepository
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
