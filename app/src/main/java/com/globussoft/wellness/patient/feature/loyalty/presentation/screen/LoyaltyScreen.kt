package com.globussoft.wellness.patient.feature.loyalty.presentation.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.loyalty.domain.model.LoyaltyData
import com.globussoft.wellness.patient.feature.loyalty.domain.model.LoyaltyTransaction
import com.globussoft.wellness.patient.feature.loyalty.presentation.state.LoyaltyUiEvent
import com.globussoft.wellness.patient.feature.loyalty.presentation.state.LoyaltyUiState

private val TealColor = Color(0xFF265855)
private val BlushColor = Color(0xFFCD9481)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyScreen(
    state: LoyaltyUiState,
    onEvent: (LoyaltyUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loyalty Points") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(LoyaltyUiEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onEvent(LoyaltyUiEvent.Refresh) }) { Text("Retry") }
                }
                state.loyaltyData != null -> LoyaltyContent(state.loyaltyData)
            }
        }
    }
}

@Composable
private fun LoyaltyContent(data: LoyaltyData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            LoyaltyBalanceCard(balance = data.balance, earnedThisMonth = data.earnedThisMonth)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (data.transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            items(data.transactions, key = { it.id }) { txn ->
                LoyaltyTransactionRow(txn)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun LoyaltyBalanceCard(balance: Int, earnedThisMonth: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = TealColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$balance",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "points",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                ),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = "Earned this month: $earnedThisMonth pts",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun LoyaltyTransactionRow(txn: LoyaltyTransaction) {
    val isEarned = txn.type.lowercase() != "redeemed"
    val pointsColor = if (isEarned) TealColor else BlushColor
    val pointsText = if (isEarned) "+${txn.points} pts" else "-${txn.points} pts"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isEarned) Icons.Default.Star else Icons.Default.RemoveCircle,
            contentDescription = null,
            tint = pointsColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = txn.reason,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = DateUtil.toDisplayDate(txn.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = pointsText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = pointsColor,
        )
    }
}
