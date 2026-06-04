package com.globussoft.wellness.patient.feature.health.domain.repository

import com.globussoft.wellness.patient.feature.health.domain.model.TreatmentPlan

interface TreatmentPlanRepository {
    suspend fun getTreatmentPlans(): List<TreatmentPlan>
}
