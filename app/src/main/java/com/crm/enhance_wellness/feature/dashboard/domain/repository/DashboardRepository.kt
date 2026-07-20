package com.crm.enhance_wellness.feature.dashboard.domain.repository

import com.crm.enhance_wellness.feature.dashboard.domain.model.Dashboard

interface DashboardRepository {
    suspend fun getDashboard(): Dashboard
}
