package com.globussoft.wellness.patient.feature.wallet.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.WellnessCard
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
                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = { onEvent(GiftCardsUiEvent.Refresh) },
                    modifier = Modifier.align(Alignment.Center),
                )
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
    WellnessCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                card.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Worth ${CurrencyUtil.formatRupees(card.amount.toDouble())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = CurrencyUtil.formatRupees(card.price.toDouble()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Valid ${card.validityDays} days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                Text(
                    purchaseError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(
                onClick = onPurchase,
                enabled = !isPurchasing,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                if (isPurchasing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Pay ${CurrencyUtil.formatRupees(card.price.toDouble())}")
            }
        }
    }
}
