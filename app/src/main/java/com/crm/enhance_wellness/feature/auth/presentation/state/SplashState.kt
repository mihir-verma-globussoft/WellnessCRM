package com.crm.enhance_wellness.feature.auth.presentation.state

import com.crm.enhance_wellness.feature.auth.domain.model.TenantBranding

data class SplashUiState(
    val isLoading: Boolean = true,
    val tenantBranding: TenantBranding? = null,
    val error: String? = null,
)
