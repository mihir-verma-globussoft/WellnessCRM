package com.crm.enhance_wellness.feature.treatmentanalysis.data.mapper

import com.crm.enhance_wellness.feature.treatmentanalysis.data.local.entity.TreatmentAnalysisDraftEntity
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisDraft
import com.crm.enhance_wellness.feature.treatmentanalysis.domain.model.TreatmentAnalysisStatus

fun TreatmentAnalysisDraftEntity.toDomain(): TreatmentAnalysisDraft =
    TreatmentAnalysisDraft(
        prescriptionId = prescriptionId,
        analysisId = analysisId,
        beforeLocalPath = beforeLocalPath,
        beforeRemoteUrl = beforeRemoteUrl,
        beforeCapturedAt = beforeCapturedAt,
        afterLocalPath = afterLocalPath,
        afterRemoteUrl = afterRemoteUrl,
        afterCapturedAt = afterCapturedAt,
        status = TreatmentAnalysisStatus.from(status),
        updatedAt = updatedAt,
    )

fun TreatmentAnalysisDraft.toEntity(): TreatmentAnalysisDraftEntity =
    TreatmentAnalysisDraftEntity(
        prescriptionId = prescriptionId,
        analysisId = analysisId,
        beforeLocalPath = beforeLocalPath,
        beforeRemoteUrl = beforeRemoteUrl,
        beforeCapturedAt = beforeCapturedAt,
        afterLocalPath = afterLocalPath,
        afterRemoteUrl = afterRemoteUrl,
        afterCapturedAt = afterCapturedAt,
        status = status.raw,
        updatedAt = updatedAt,
    )
