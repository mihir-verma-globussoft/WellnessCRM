package com.crm.enhance_wellness.feature.health.data.repository

import com.crm.enhance_wellness.core.network.WellnessApiService
import com.crm.enhance_wellness.feature.health.data.local.entity.CachedPrescription
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionDao
import com.crm.enhance_wellness.feature.health.data.mapper.toDomain
import com.crm.enhance_wellness.feature.health.data.mapper.toEntity
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val dao: PrescriptionDao,
) : PrescriptionRepository {

    override suspend fun getPrescriptions(): List<Prescription> {
        val response = api.getPrescriptions()
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body() ?: throw HttpException(response)
        val prescriptions = body.map { it.toDomain() }
        dao.insertAll(prescriptions.map { it.toEntity() })
        return prescriptions
    }

    override suspend fun getCachedPrescriptions(): List<Prescription> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getPrescriptionPdf(prescriptionId: Int): ByteArray {
        val response = api.getPrescriptionPdf(prescriptionId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()?.bytes() ?: throw HttpException(response)
    }

    override suspend fun savePdfToCache(prescriptionId: Int, pdfBytes: ByteArray) {
        val existing = dao.getById(prescriptionId)
        if (existing != null) {
            dao.insert(existing.copy(pdfBytes = pdfBytes, pdfCachedAt = System.currentTimeMillis()))
        } else {
            dao.insert(
                CachedPrescription(
                    id = prescriptionId,
                    visitId = null,
                    visitDate = 0L,
                    doctorName = null,
                    serviceName = null,
                    drugCount = 0,
                    pdfBytes = pdfBytes,
                    pdfCachedAt = System.currentTimeMillis(),
                    cachedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    override suspend fun getCachedPdf(prescriptionId: Int): ByteArray? =
        dao.getById(prescriptionId)?.pdfBytes

    override suspend fun evictStalePdfs() {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        dao.evictStalePdfs(sevenDaysAgo)
    }
}
