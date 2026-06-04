package com.globussoft.wellness.patient.feature.membership.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.membership.domain.model.Membership
import com.globussoft.wellness.patient.feature.membership.domain.model.MembershipPlan
import com.globussoft.wellness.patient.feature.membership.presentation.state.MembershipsUiEvent
import com.globussoft.wellness.patient.feature.membership.presentation.state.MembershipsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipsScreen(
    state: MembershipsUiState,
    onEvent: (MembershipsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memberships") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MembershipsUiEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onEvent(MembershipsUiEvent.TogglePlans) }) {
                        Text(if (state.showPlans) "My Memberships" else "Browse Plans")
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
                    Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { onEvent(MembershipsUiEvent.Refresh) }) { Text("Retry") }
                }
                state.showPlans -> PlanCatalog(plans = state.plans)
                else -> MyMembershipsList(
                    memberships = state.memberships,
                    onSelect = { onEvent(MembershipsUiEvent.SelectMembership(it)) },
                )
            }
        }
    }

    state.selectedMembership?.let { membership ->
        MembershipDetailSheet(
            membership = membership,
            onDismiss = { onEvent(MembershipsUiEvent.DismissDetail) },
        )
    }
}

@Composable
private fun MyMembershipsList(memberships: List<Membership>, onSelect: (Membership) -> Unit) {
    if (memberships.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No memberships found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(memberships) { m ->
            MembershipCard(membership = m, onClick = { onSelect(m) })
        }
    }
}

@Composable
private fun MembershipCard(membership: Membership, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(membership.planName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusBadge(status = membership.status)
            }
            Text("Valid until ${DateUtil.toDisplayDate(membership.endDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (membership.balance.isNotEmpty()) {
                membership.balance.forEach { b ->
                    if (b.serviceName != null && b.remaining != null && b.total != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(b.serviceName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${b.remaining}/${b.total}", style = MaterialTheme.typography.labelSmall)
                            }
                            LinearProgressIndicator(
                                progress = { if (b.total > 0) b.remaining.toFloat() / b.total else 0f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "active" -> MaterialTheme.colorScheme.primary
        "expired" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.15f)) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PlanCatalog(plans: List<MembershipPlan>) {
    if (plans.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No plans available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(plans) { plan ->
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(plan.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (!plan.description.isNullOrBlank()) {
                        Text(plan.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(CurrencyUtil.formatRupees(plan.price), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("${plan.durationDays} days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembershipDetailSheet(membership: Membership, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(membership.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            DetailRow("Status", membership.status)
            DetailRow("Start date", DateUtil.toDisplayDate(membership.startDate))
            DetailRow("End date", DateUtil.toDisplayDate(membership.endDate))
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
