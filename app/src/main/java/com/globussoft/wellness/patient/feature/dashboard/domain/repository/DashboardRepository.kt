package com.globussoft.wellness.patient.feature.dashboard.domain.repository

import com.globussoft.wellness.patient.feature.dashboard.domain.model.Dashboard

interface DashboardRepository {
    suspend fun getDashboard(): Dashboard
}
