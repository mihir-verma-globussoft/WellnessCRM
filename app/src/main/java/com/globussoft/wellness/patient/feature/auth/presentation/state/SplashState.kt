package com.globussoft.wellness.patient.feature.auth.presentation.state

import com.globussoft.wellness.patient.feature.auth.domain.model.TenantBranding

data class SplashUiState(
    val isLoading: Boolean = true,
    val tenantBranding: TenantBranding? = null,
    val error: String? = null,
)
