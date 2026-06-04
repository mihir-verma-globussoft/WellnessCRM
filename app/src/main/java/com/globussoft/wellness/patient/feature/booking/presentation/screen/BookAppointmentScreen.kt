package com.globussoft.wellness.patient.feature.booking.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.globussoft.wellness.patient.feature.booking.domain.model.Product
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    state: BookAppointmentUiState,
    onEvent: (BookAppointmentUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.step) {
                            1 -> "Select Service"
                            2 -> "Choose Date & Time"
                            else -> "Review & Confirm"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BookAppointmentUiEvent.PreviousStep) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LinearProgressIndicator(
                progress = { state.step / 3f },
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null && state.products.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text(text = state.error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { onEvent(BookAppointmentUiEvent.LoadProducts) }) { Text("Retry") }
                    }
                }
                else -> when (state.step) {
                    1 -> Step1Products(state = state, onEvent = onEvent)
                    2 -> Step2DateTime(state = state, onEvent = onEvent)
                    else -> Step3Confirm(state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun Step1Products(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Choose a service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        if (state.products.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No services available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(state.products) { product ->
                    ProductCard(
                        product = product,
                        isSelected = state.selectedProduct?.id == product.id,
                        onClick = { onEvent(BookAppointmentUiEvent.SelectProduct(product)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onEvent(BookAppointmentUiEvent.NextStep) },
                enabled = state.selectedProduct != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Continue") }
            if (state.error != null) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (!product.categoryName.isNullOrBlank()) {
                Text(text = product.categoryName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (product.price != null) {
                Text(text = CurrencyUtil.formatRupees(product.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun Step2DateTime(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    val timeSlots = remember {
        listOf("09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
               "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // Simple date offset picker (today + 30 days)
        val today = remember { Calendar.getInstance() }
        val dateOptions = remember {
            (0..29).map { offset ->
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
                Pair(cal.timeInMillis, DateUtil.toDisplayDate(cal.timeInMillis))
            }
        }
        var dateScrollPos by remember { mutableIntStateOf(0) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dateOptions.take(7).forEachIndexed { i, (ms, label) ->
                FilterChip(
                    selected = state.selectedDate == ms,
                    onClick = { onEvent(BookAppointmentUiEvent.SelectDate(ms)) },
                    label = { Text(label.take(6), style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Select time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(timeSlots) { time ->
                FilterChip(
                    selected = state.selectedTime == time,
                    onClick = { onEvent(BookAppointmentUiEvent.SelectTime(time)) },
                    label = { Text(time) },
                )
            }
        }

        if (state.error != null) {
            Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onEvent(BookAppointmentUiEvent.NextStep) },
            enabled = state.selectedDate != null && state.selectedTime != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Continue") }
    }
}

@Composable
private fun Step3Confirm(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Confirm booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Service", state.selectedProduct?.name ?: "—")
                InfoRow("Date", DateUtil.toDisplayDate(state.selectedDate ?: 0L))
                InfoRow("Time", state.selectedTime ?: "—")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Reason for visit", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = state.reason,
            onValueChange = { onEvent(BookAppointmentUiEvent.EnterReason(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Routine checkup, follow-up…") },
            minLines = 3,
            maxLines = 5,
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onEvent(BookAppointmentUiEvent.ConfirmBooking) },
            enabled = !state.isBooking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isBooking) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Book Appointment")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
