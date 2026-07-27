package com.crm.enhance_wellness.feature.treatmentanalysis.domain.model

data class TreatmentAnalysisDraft(
    val prescriptionId: Int,
    val analysisId: Int?,
    val beforeLocalPath: String?,
    val beforeRemoteUrl: String?,
    val beforeCapturedAt: Long?,
    val afterLocalPath: String?,
    val afterRemoteUrl: String?,
    val afterCapturedAt: Long?,
    val status: TreatmentAnalysisStatus,
    val updatedAt: Long,
) {
    val hasBefore: Boolean = !beforeRemoteUrl.isNullOrBlank() || !beforeLocalPath.isNullOrBlank()
    val hasUploadedBefore: Boolean = status == TreatmentAnalysisStatus.BEFORE_UPLOADED ||
        status == TreatmentAnalysisStatus.AFTER_CAPTURED ||
        status == TreatmentAnalysisStatus.SUBMITTED_FOR_REVIEW ||
        !beforeRemoteUrl.isNullOrBlank()
    val hasSubmittedAfter: Boolean = status == TreatmentAnalysisStatus.SUBMITTED_FOR_REVIEW ||
        !afterRemoteUrl.isNullOrBlank()
}

enum class TreatmentAnalysisStatus(val raw: String) {
    DRAFT("draft"),
    BEFORE_CAPTURED("before_captured"),
    BEFORE_UPLOADED("before_uploaded"),
    AFTER_CAPTURED("after_captured"),
    SUBMITTED_FOR_REVIEW("submitted_for_review");

    companion object {
        fun from(raw: String?): TreatmentAnalysisStatus =
            entries.firstOrNull { it.raw == raw } ?: DRAFT
    }
}
