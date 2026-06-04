package com.globussoft.wellness.patient.feature.dashboard.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.SectionLabel
import com.globussoft.wellness.patient.core.ui.StatusChip
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.dashboard.domain.model.UpcomingVisit
import com.globussoft.wellness.patient.feature.dashboard.presentation.state.DashboardUiEvent
import com.globussoft.wellness.patient.feature.dashboard.presentation.state.DashboardUiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val greeting = remember {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        when {
                            hour < 12 -> "Good morning"
                            hour < 17 -> "Good afternoon"
                            else -> "Good evening"
                        }
                    }
                    val name = state.dashboard?.patientName.orEmpty()
                    Column {
                        Text(
                            text = if (name.isNotBlank()) "$greeting, $name" else greeting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(DashboardUiEvent.NavigateToNotifications) }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = { onEvent(DashboardUiEvent.Refresh) },
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> DashboardContent(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.dashboard?.nextVisit != null) {
            NextVisitCard(
                visit = state.dashboard.nextVisit,
                onClick = { onEvent(DashboardUiEvent.NavigateToAppointments) },
            )
        } else {
            NoUpcomingVisitCard(
                onBookClick = { onEvent(DashboardUiEvent.NavigateToBooking) },
            )
        }

        SectionLabel(text = "Overview")
        StatRow(
            walletBalance = state.dashboard?.walletBalance,
            walletCurrency = state.dashboard?.walletCurrency,
            membershipCount = state.dashboard?.activeMembershipCount ?: 0,
            loyaltyPoints = state.dashboard?.loyaltyPoints,
            onWalletClick = { onEvent(DashboardUiEvent.NavigateToWallet) },
            onMembershipsClick = { onEvent(DashboardUiEvent.NavigateToMemberships) },
            onLoyaltyClick = { onEvent(DashboardUiEvent.NavigateToLoyalty) },
        )

        SectionLabel(text = "Quick actions")
        QuickActionsRow(onEvent = onEvent)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NextVisitCard(visit: UpcomingVisit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Next appointment",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                StatusChip(status = visit.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = DateUtil.toDisplayDate(visit.visitDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (!visit.serviceName.isNullOrBlank()) {
                Text(
                    text = visit.serviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (!visit.doctorName.isNullOrBlank()) {
                Text(
                    text = "with ${visit.doctorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun NoUpcomingVisitCard(onBookClick: () -> Unit) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "No upcoming appointments",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Book a visit today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onBookClick) {
                Text("Book now")
            }
        }
    }
}

@Composable
private fun StatRow(
    walletBalance: Long?,
    walletCurrency: String?,
    membershipCount: Int,
    loyaltyPoints: Int?,
    onWalletClick: () -> Unit,
    onMembershipsClick: () -> Unit,
    onLoyaltyClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            label = "Wallet",
            value = if (walletBalance != null) {
                CurrencyUtil.formatPaise(walletBalance, walletCurrency ?: "INR")
            } else "—",
            icon = Icons.Default.AccountBalanceWallet,
            onClick = onWalletClick,
        )
        StatChip(
            modifier = Modifier.weight(1f),
            label = "Members",
            value = membershipCount.toString(),
            icon = Icons.Default.CardMembership,
            onClick = onMembershipsClick,
        )
        StatChip(
            modifier = Modifier.weight(1f),
            label = "Loyalty",
            value = if (loyaltyPoints != null) "$loyaltyPoints pts" else "—",
            icon = Icons.Default.Star,
            onClick = onLoyaltyClick,
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WellnessCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickActionsRow(onEvent: (DashboardUiEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            label = "Book",
            icon = Icons.Default.CalendarToday,
            onClick = { onEvent(DashboardUiEvent.NavigateToBooking) },
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            label = "Rx",
            icon = Icons.Default.MedicalServices,
            onClick = { onEvent(DashboardUiEvent.NavigateToPrescriptions) },
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            label = "Gift Cards",
            icon = Icons.Default.CardGiftcard,
            onClick = { onEvent(DashboardUiEvent.NavigateToGiftCards) },
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            label = "Profile",
            icon = Icons.Default.Person,
            onClick = { onEvent(DashboardUiEvent.NavigateToProfile) },
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WellnessCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
