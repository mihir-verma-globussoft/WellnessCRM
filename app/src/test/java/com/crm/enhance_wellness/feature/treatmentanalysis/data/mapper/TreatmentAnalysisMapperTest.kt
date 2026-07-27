package com.crm.enhance_wellness.feature.treatmentanalysis.data.mapper

import com.crm.enhance_wellness.feature.treatmentanalysis.data.local.entity.TreatmentAnalysisDraftEntity
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TreatmentAnalysisMapperTest {

    @Test
    fun `status parser defaults unknown values to draft`() {
        assertEquals(TreatmentAnalysisStatus.BEFORE_UPLOADED, TreatmentAnalysisStatus.from("before_uploaded"))
        assertEquals(TreatmentAnalysisStatus.DRAFT, TreatmentAnalysisStatus.from("unknown"))
        assertEquals(TreatmentAnalysisStatus.DRAFT, TreatmentAnalysisStatus.from(null))
    }

    @Test
    fun `entity round trip preserves uploaded before state`() {
        val draft = TreatmentAnalysisDraft(
            prescriptionId = 12,
            analysisId = null,
            beforeLocalPath = "/local/before.jpg",
            beforeRemoteUrl = "",
            beforeCapturedAt = 100L,
            afterLocalPath = null,
            afterRemoteUrl = null,
            afterCapturedAt = null,
            status = TreatmentAnalysisStatus.BEFORE_UPLOADED,
            updatedAt = 100L,
        )

        val entity: TreatmentAnalysisDraftEntity = draft.toEntity()
        val restored = entity.toDomain()

        assertEquals("/local/before.jpg", restored.beforeLocalPath)
        assertEquals(TreatmentAnalysisStatus.BEFORE_UPLOADED, restored.status)
        assertTrue(restored.hasUploadedBefore)
    }
}
