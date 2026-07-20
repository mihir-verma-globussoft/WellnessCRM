package com.crm.enhance_wellness.feature.health.domain.repository

import com.crm.enhance_wellness.feature.health.domain.model.Prescription

interface PrescriptionRepository {
    suspend fun getPrescriptions(): List<Prescription>
    suspend fun getCachedPrescriptions(): List<Prescription>
    suspend fun getPrescriptionPdf(prescriptionId: Int): ByteArray
    suspend fun savePdfToCache(prescriptionId: Int, pdfBytes: ByteArray)
    suspend fun getCachedPdf(prescriptionId: Int): ByteArray?
    suspend fun evictStalePdfs()
}
