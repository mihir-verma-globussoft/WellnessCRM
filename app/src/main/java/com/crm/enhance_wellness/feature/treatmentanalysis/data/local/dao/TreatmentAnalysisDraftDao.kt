package com.crm.enhance_wellness.feature.treatmentanalysis.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.crm.enhance_wellness.feature.treatmentanalysis.data.local.entity.TreatmentAnalysisDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentAnalysisDraftDao {
    @Query("SELECT * FROM treatment_analysis_drafts WHERE prescriptionId = :prescriptionId")
    fun observe(prescriptionId: Int): Flow<TreatmentAnalysisDraftEntity?>

    @Query("SELECT * FROM treatment_analysis_drafts WHERE prescriptionId = :prescriptionId")
    suspend fun get(prescriptionId: Int): TreatmentAnalysisDraftEntity?

    @Upsert
    suspend fun upsert(entity: TreatmentAnalysisDraftEntity)
}
