package com.globussoft.wellness.patient.feature.wallet.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.globussoft.wellness.patient.feature.wallet.domain.model.GiftCard
import com.globussoft.wellness.patient.feature.wallet.presentation.state.GiftCardsUiEvent
import com.globussoft.wellness.patient.feature.wallet.presentation.state.GiftCardsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftCardsScreen(
    state: GiftCardsUiState,
    onEvent: (GiftCardsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gift Cards") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(GiftCardsUiEvent.NavigateBack) }) {
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
                    Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { onEvent(GiftCardsUiEvent.Refresh) }) { Text("Retry") }
                }
                state.giftCards.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No gift cards available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.giftCards) { card ->
                        GiftCardItem(card = card, onClick = { onEvent(GiftCardsUiEvent.SelectCard(card)) })
                    }
                }
            }
        }
    }

    state.selectedCard?.let { card ->
        GiftCardPurchaseSheet(
            card = card,
            isPurchasing = state.isPurchasing,
            purchaseError = state.purchaseError,
            onDismiss = { onEvent(GiftCardsUiEvent.DismissCard) },
            onPurchase = { onEvent(GiftCardsUiEvent.InitiatePurchase(card.id)) },
        )
    }
}

@Composable
private fun GiftCardItem(card: GiftCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                text = "Worth ${CurrencyUtil.formatRupees(card.amount.toDouble())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = CurrencyUtil.formatRupees(card.price.toDouble()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text("Valid ${card.validityDays} days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GiftCardPurchaseSheet(
    card: GiftCard,
    isPurchasing: Boolean,
    purchaseError: String?,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(card.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Value", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyUtil.formatRupees(card.amount.toDouble()), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyUtil.formatRupees(card.price.toDouble()), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Validity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${card.validityDays} days", fontWeight = FontWeight.Medium)
            }
            if (purchaseError != null) {
                Text(purchaseError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onPurchase,
                enabled = !isPurchasing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isPurchasing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Pay ${CurrencyUtil.formatRupees(card.price.toDouble())}")
            }
        }
    }
}
