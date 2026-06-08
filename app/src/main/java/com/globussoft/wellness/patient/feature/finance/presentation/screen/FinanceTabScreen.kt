package com.globussoft.wellness.patient.feature.finance.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.EmptyState
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.StatusChip
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.feature.finance.domain.model.Payment
import com.globussoft.wellness.patient.feature.finance.presentation.state.FinanceUiEvent
import com.globussoft.wellness.patient.feature.finance.presentation.state.FinanceUiState

private val TAB_LABELS = listOf("Payments", "Gift Cards", "Transactions")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTabScreen(
    state: FinanceUiState,
    onEvent: (FinanceUiEvent) -> Unit,
    onNavigateToGiftCards: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Navigate and reset when tabs 1 or 2 are tapped
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> {
                onNavigateToGiftCards()
                selectedTab = 0
            }
            2 -> {
                onNavigateToWallet()
                selectedTab = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
        ) {
            TAB_LABELS.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) },
                )
            }
        }

        when (selectedTab) {
            0 -> PaymentsContent(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentsContent(
    state: FinanceUiState,
    onEvent: (FinanceUiEvent) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onEvent(FinanceUiEvent.LoadPayments) },
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading && state.payments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(FinanceUiEvent.LoadPayments) },
                    )
                }
            }

            state.payments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(message = "No payments yet.")
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        PaymentsKpiRow(payments = state.payments)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(state.payments) { payment ->
                        PaymentCard(payment = payment)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsKpiRow(payments: List<Payment>) {
    val totalPaid = payments
        .filter { it.status.equals("paid", ignoreCase = true) || it.status.equals("success", ignoreCase = true) }
        .sumOf { it.amount }
    val pendingCount = payments.count { it.status.equals("pending", ignoreCase = true) }
    val failedCount = payments.count { it.status.equals("failed", ignoreCase = true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KpiCard(
            modifier = Modifier.weight(1f),
            label = "Total Paid",
            value = CurrencyUtil.formatPaise(totalPaid),
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            label = "Pending",
            value = pendingCount.toString(),
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            label = "Failed",
            value = failedCount.toString(),
        )
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    WellnessCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaymentCard(payment: Payment) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = CurrencyUtil.formatPaise(payment.amount, payment.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StatusChip(status = payment.status)
            }

            if (!payment.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = payment.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDate(payment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!payment.gateway.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    GatewayBadge(gateway = payment.gateway)
                }
            }
        }
    }
}

@Composable
private fun GatewayBadge(gateway: String) {
    val label = when (gateway.lowercase()) {
        "razorpay" -> "Razorpay"
        "stripe" -> "Stripe"
        else -> gateway.replaceFirstChar { it.uppercase() }
    }
    Text(
        text = "· $label",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
    )
}

/**
 * Formats an ISO-8601 date string to "DD MMM YYYY".
 * Falls back to the raw string on any parse error.
 */
private fun formatDate(iso: String): String {
    return try {
        val parts = iso.substringBefore('T').split("-")
        if (parts.size < 3) return iso
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val month = parts[1].toIntOrNull() ?: return iso
        "${parts[2]} ${months.getOrElse(month) { parts[1] }} ${parts[0]}"
    } catch (_: Exception) {
        iso
    }
}
