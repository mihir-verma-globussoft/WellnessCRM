package com.globussoft.wellness.patient.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.theme.parseBrandColor
import com.globussoft.wellness.patient.feature.auth.presentation.state.SplashUiState

@Composable
fun SplashScreen(
    state: SplashUiState,
) {
    val brandColor = state.tenantBranding?.brandColor
        ?.let { parseBrandColor(it) }
        ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(brandColor, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.tenantBranding?.name?.take(1)?.uppercase() ?: "W",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.tenantBranding?.name ?: "Wellness",
                style = MaterialTheme.typography.headlineMedium,
                color = brandColor,
                textAlign = TextAlign.Center,
            )

            if (state.tenantBranding?.tagline != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.tenantBranding.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = brandColor)
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
