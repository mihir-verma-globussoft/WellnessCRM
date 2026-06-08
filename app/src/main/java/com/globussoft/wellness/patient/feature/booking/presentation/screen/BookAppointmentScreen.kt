package com.globussoft.wellness.patient.feature.booking.presentation.screen

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.core.util.CurrencyUtil
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.booking.domain.model.Product
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.BookAppointmentUiState
import com.globussoft.wellness.patient.feature.booking.presentation.state.DoctorOption
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    state: BookAppointmentUiState,
    onEvent: (BookAppointmentUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LinearProgressIndicator(
            progress = { state.step / 4f },
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null && state.products.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = state.error,
                    onRetry = { onEvent(BookAppointmentUiEvent.LoadProducts) },
                )
            }
            else -> when (state.step) {
                1 -> Step1Products(state = state, onEvent = onEvent)
                2 -> Step2DoctorSelection(state = state, onEvent = onEvent)
                3 -> Step3DateTime(state = state, onEvent = onEvent)
                else -> Step4Confirm(state = state, onEvent = onEvent)
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
                shape = MaterialTheme.shapes.extraLarge,
            ) { Text("Continue") }
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, isSelected: Boolean, onClick: () -> Unit) {
    if (isSelected) {
        androidx.compose.material3.Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = CardDefaults.outlinedCardBorder(),
        ) {
            ProductCardContent(product = product, isSelected = true)
        }
    } else {
        WellnessCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
            ProductCardContent(product = product, isSelected = false)
        }
    }
}

@Composable
private fun ProductCardContent(product: Product, isSelected: Boolean) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
        )
        if (!product.categoryName.isNullOrBlank()) {
            Text(
                text = product.categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (product.price != null) {
            Text(
                text = CurrencyUtil.formatRupees(product.price),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Step2DoctorSelection(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    val doctors = if (state.doctors.isEmpty()) {
        listOf(DoctorOption(id = null, name = "No preference"))
    } else {
        state.doctors
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Choose a doctor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        WellnessCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "If you choose no preference, the clinic will assign a doctor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(doctors) { doc ->
                val isSelected = state.selectedDoctorId == doc.id
                WellnessCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(BookAppointmentUiEvent.SelectDoctor(doc.id)) },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = doc.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        if (isSelected) {
                            Text(
                                "✓",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onEvent(BookAppointmentUiEvent.NextStep) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) { Text("Continue") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step3DateTime(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    val timeSlots = remember {
        listOf(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00",
        )
    }
    val dateOptions = remember {
        (0..29).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
            Pair(cal.timeInMillis, DateUtil.toDisplayDate(cal.timeInMillis))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dateOptions.take(7).forEach { (ms, label) ->
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
            shape = MaterialTheme.shapes.extraLarge,
        ) { Text("Continue") }
    }
}

@Composable
private fun Step4Confirm(state: BookAppointmentUiState, onEvent: (BookAppointmentUiEvent) -> Unit) {
    val doctorLabel = state.doctors.find { it.id == state.selectedDoctorId }?.name ?: "No preference"
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Confirm booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        WellnessCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Service", state.selectedProduct?.name ?: "—")
                InfoRow("Doctor", doctorLabel)
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
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            if (state.isBooking) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Book Appointment")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}
