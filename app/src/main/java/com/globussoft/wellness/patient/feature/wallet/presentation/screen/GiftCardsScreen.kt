package com.globussoft.wellness.patient.feature.wallet.presentation.screen

import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.feature.wallet.presentation.state.GiftCardsUiEvent
import com.globussoft.wellness.patient.feature.wallet.presentation.state.GiftCardsUiState

private val DEMO_DENOMINATIONS = listOf(500, 1_000, 2_000, 5_000, 10_000)

private val CARD_COLORS = listOf(
    Color(0xFF265855),
    Color(0xFF7B5B0D),
    Color(0xFF1B2E4B),
    Color(0xFF4A3470),
    Color(0xFF8B1A2C),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftCardsScreen(
    state: GiftCardsUiState,
    onEvent: (GiftCardsUiEvent) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showBuySheet by remember { mutableStateOf(false) }
    var selectedDenom by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val filteredDenoms = remember(searchQuery) {
        DEMO_DENOMINATIONS.filter { denom ->
            searchQuery.isBlank() || denom.toString().contains(searchQuery.trim())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Dr. Haror's Wellness Gift Cards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Give the gift of wellness — redeemable for any service or session.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by amount…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        }

        if (filteredDenoms.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No gift cards match \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(filteredDenoms) { index, denom ->
                    DemoGiftCard(
                        amount = denom,
                        color = CARD_COLORS[index % CARD_COLORS.size],
                        onBuy = {
                            selectedDenom = denom
                            showBuySheet = true
                        },
                    )
                }
            }
        }
    }

    if (showBuySheet) {
        ModalBottomSheet(
            onDismissRequest = { showBuySheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    "Confirm Purchase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Gift Card Value", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyUtil.formatRupees(selectedDenom.toDouble()), fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Validity", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("365 days", fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Redeemable at", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dr. Haror's Wellness", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        showBuySheet = false
                        Toast.makeText(context, "Gift card purchase — contact clinic to complete", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Text("Buy ${CurrencyUtil.formatRupees(selectedDenom.toDouble())}")
                }
            }
        }
    }
}

@Composable
private fun DemoGiftCard(amount: Int, color: Color, onBuy: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = color,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dr. Haror's\nWellness",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 14.sp,
                )
                AssistChip(
                    onClick = {},
                    label = { Text("e-Gift", style = MaterialTheme.typography.labelSmall, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.18f)),
                    border = null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(start = 2.dp),
                        )
                    },
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = CurrencyUtil.formatRupees(amount.toDouble()),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )

            Text(
                text = "Valid 365 days",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.70f),
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = color,
                ),
            ) {
                Text("Buy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
