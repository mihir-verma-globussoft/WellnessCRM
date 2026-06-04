package com.globussoft.wellness.patient.feature.health.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.health.domain.model.Prescription
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionsUiEvent
import com.globussoft.wellness.patient.feature.health.presentation.state.PrescriptionsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionsScreen(
    state: PrescriptionsUiState,
    onEvent: (PrescriptionsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescriptions") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PrescriptionsUiEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = { onEvent(PrescriptionsUiEvent.Refresh) },
                    modifier = Modifier.align(Alignment.Center),
                )
                state.prescriptions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No prescriptions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.prescriptions) { prescription ->
                        PrescriptionCard(
                            prescription = prescription,
                            onClick = { onEvent(PrescriptionsUiEvent.ViewPdf(prescription.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionCard(prescription: Prescription, onClick: () -> Unit) {
    WellnessCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prescription.serviceName ?: "Prescription",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                if (!prescription.doctorName.isNullOrBlank()) {
                    Text(
                        text = prescription.doctorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = DateUtil.toDisplayDate(prescription.visitDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                // Drug count badge with secondaryContainer (mint)
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "${prescription.drugs.size} medication${if (prescription.drugs.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = "View PDF",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
