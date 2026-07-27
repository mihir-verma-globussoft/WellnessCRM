package com.crm.enhance_wellness.feature.treatmentanalysis.domain.repository

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TreatmentAnalysisRepository {
    fun observeDraft(prescriptionId: Int): Flow<TreatmentAnalysisDraft?>
    suspend fun saveBeforeCapture(prescriptionId: Int, imageFile: File): Result<TreatmentAnalysisDraft>
    suspend fun saveAfterCapture(prescriptionId: Int, imageFile: File): Result<TreatmentAnalysisDraft>
    suspend fun uploadBefore(prescriptionId: Int, visitId: Int): Result<TreatmentAnalysisDraft>
    suspend fun uploadAfter(prescriptionId: Int, visitId: Int): Result<TreatmentAnalysisDraft>
}
