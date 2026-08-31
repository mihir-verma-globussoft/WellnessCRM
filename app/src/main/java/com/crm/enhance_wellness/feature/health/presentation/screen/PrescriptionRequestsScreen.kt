package com.crm.enhance_wellness.feature.health.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crm.enhance_wellness.core.ui.ErrorState
import com.crm.enhance_wellness.core.ui.StatusChip
import com.crm.enhance_wellness.core.ui.WellnessCard
import com.crm.enhance_wellness.core.util.DateUtil
import com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest
import com.crm.enhance_wellness.feature.health.domain.model.toRefillStatusLabel
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionRequestsUiEvent
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionRequestsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionRequestsScreen(
    state: PrescriptionRequestsUiState,
    onEvent: (PrescriptionRequestsUiEvent) -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) isRefreshing = false }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true; onEvent(PrescriptionRequestsUiEvent.Refresh) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null -> ErrorState(
                message = state.error,
                onRetry = { onEvent(PrescriptionRequestsUiEvent.Refresh) },
                modifier = Modifier.align(Alignment.Center),
            )
            state.requests.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No medicine or refill requests found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.requests, key = { it.id }) { request ->
                    PrescriptionRequestCard(request)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionRequestCard(request: PrescriptionRequest) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.drugNames.ifEmpty { listOf("Prescription #${request.prescriptionId}") }
                            .joinToString(", "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!request.doctorName.isNullOrBlank()) {
                        Text(
                            text = request.doctorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                StatusChip(status = request.status.toRefillStatusLabel())
            }

            Text(
                text = if (request.isFullPrescription) "Full prescription refill" else "Partial refill",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "Requested: ${DateUtil.toDisplayDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (request.requestedDurationDays != null) {
                Text(
                    text = "Requested duration: ${request.requestedDurationDays} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!request.notes.isNullOrBlank()) {
                Text(
                    text = "Note: ${request.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!request.reviewedByName.isNullOrBlank()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Reviewed by ${request.reviewedByName}" +
                            request.reviewedAt?.let { " · ${DateUtil.toDisplayDate(it)}" }.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!request.reviewNote.isNullOrBlank()) {
                        Text(
                            text = request.reviewNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
