package com.crm.enhance_wellness.feature.treatmentanalysis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treatment_analysis_drafts")
data class TreatmentAnalysisDraftEntity(
    @PrimaryKey val prescriptionId: Int,
    val analysisId: Int?,
    val beforeLocalPath: String?,
    val beforeRemoteUrl: String?,
    val beforeCapturedAt: Long?,
    val afterLocalPath: String?,
    val afterRemoteUrl: String?,
    val afterCapturedAt: Long?,
    val status: String,
    val updatedAt: Long,
)
