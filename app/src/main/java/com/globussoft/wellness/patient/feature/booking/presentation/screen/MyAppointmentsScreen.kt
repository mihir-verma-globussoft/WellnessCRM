package com.globussoft.wellness.patient.feature.booking.presentation.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.StatusChip
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.booking.domain.model.Appointment
import com.globussoft.wellness.patient.feature.booking.presentation.state.MyAppointmentsUiEvent
import com.globussoft.wellness.patient.feature.booking.presentation.state.MyAppointmentsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(
    state: MyAppointmentsUiState,
    onEvent: (MyAppointmentsUiEvent) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MyAppointmentsUiEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(MyAppointmentsUiEvent.NavigateToHistory) }) {
                        Icon(Icons.Default.History, contentDescription = "Visit history")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(MyAppointmentsUiEvent.NavigateToBook) }) {
                Icon(Icons.Default.Add, contentDescription = "Book appointment")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Upcoming") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Past") })
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(MyAppointmentsUiEvent.Refresh) },
                    )
                }
                else -> {
                    val list = if (selectedTab == 0) state.upcoming else state.past
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedTab == 0) "No upcoming appointments" else "No past appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(list) { appt ->
                                AppointmentCard(
                                    appointment = appt,
                                    isCancelling = state.cancellingId == appt.id,
                                    showCancel = selectedTab == 0 && appt.status != "cancelled",
                                    onCancel = { onEvent(MyAppointmentsUiEvent.Cancel(appt.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    isCancelling: Boolean,
    showCancel: Boolean,
    onCancel: () -> Unit,
) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.serviceName ?: "Appointment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!appointment.doctorName.isNullOrBlank()) {
                        Text(
                            text = "with ${appointment.doctorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                StatusChip(status = appointment.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = DateUtil.toDisplayDateTime(appointment.appointmentDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (!appointment.reason.isNullOrBlank()) {
                Text(
                    text = appointment.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showCancel) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isCancelling,
                    modifier = Modifier.align(Alignment.End),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
