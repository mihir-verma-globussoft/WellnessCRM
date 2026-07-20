package com.crm.enhance_wellness.feature.health.domain.repository

import com.crm.enhance_wellness.feature.health.domain.model.TreatmentPlan

interface TreatmentPlanRepository {
    suspend fun getTreatmentPlans(): List<TreatmentPlan>
}
