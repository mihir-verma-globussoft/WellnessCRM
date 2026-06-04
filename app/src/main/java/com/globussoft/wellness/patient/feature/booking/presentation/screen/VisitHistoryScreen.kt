package com.globussoft.wellness.patient.feature.booking.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.booking.domain.model.Visit
import com.globussoft.wellness.patient.feature.booking.presentation.state.VisitHistoryUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.VisitHistoryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    state: VisitHistoryUiState,
    onEvent: (VisitHistoryUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit History") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(VisitHistoryUiEvent.NavigateBack) }) {
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
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Text(text = state.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { onEvent(VisitHistoryUiEvent.Refresh) }) { Text("Retry") }
                }
                state.visits.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No visits yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Group by month
                    val grouped = state.visits.groupBy { DateUtil.toDisplayMonthYear(it.visitDate) }
                    grouped.forEach { (monthLabel, visits) ->
                        item {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        items(visits) { visit ->
                            VisitCard(visit = visit, onClick = { onEvent(VisitHistoryUiEvent.SelectVisit(visit)) })
                        }
                    }
                }
            }
        }
    }

    state.selectedVisit?.let { visit ->
        VisitDetailSheet(visit = visit, onDismiss = { onEvent(VisitHistoryUiEvent.DismissDetail) })
    }
}

@Composable
private fun VisitCard(visit: Visit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visit.serviceName ?: "Visit",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                if (!visit.doctorName.isNullOrBlank()) {
                    Text(
                        text = visit.doctorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = DateUtil.toDisplayDate(visit.visitDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (visit.amountCharged != null) {
                Text(
                    text = CurrencyUtil.formatRupees(visit.amountCharged),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDetailSheet(visit: Visit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Visit Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            DetailRow("Service", visit.serviceName ?: "—")
            DetailRow("Doctor", visit.doctorName ?: "Not assigned")
            DetailRow("Date", DateUtil.toDisplayDate(visit.visitDate))
            DetailRow("Status", visit.status)
            if (visit.locationName != null) DetailRow("Location", visit.locationName)
            if (visit.bookingType != null) DetailRow("Type", visit.bookingType)
            if (visit.amountCharged != null) DetailRow("Amount", CurrencyUtil.formatRupees(visit.amountCharged))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
