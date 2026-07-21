package com.crm.enhance_wellness.feature.health.presentation.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crm.enhance_wellness.core.ui.ErrorState
import com.crm.enhance_wellness.core.ui.WellnessCard
import com.crm.enhance_wellness.core.util.DateUtil
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiEvent
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionsScreen(
    state: PrescriptionsUiState,
    onEvent: (PrescriptionsUiEvent) -> Unit,
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) isRefreshing = false }
    LaunchedEffect(state.reminderMessage) {
        state.reminderMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onEvent(PrescriptionsUiEvent.DismissReminderMessage)
        }
    }
    LaunchedEffect(state.exactAlarmPermissionPromptNeeded) {
        if (state.exactAlarmPermissionPromptNeeded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            runCatching { context.startActivity(intent) }
            onEvent(PrescriptionsUiEvent.ExactAlarmPermissionPromptShown)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true; onEvent(PrescriptionsUiEvent.Refresh) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.permissionBlocked -> ErrorState(
                message = "Prescription access is not enabled for your account. Contact your clinic.",
                onRetry = { onEvent(PrescriptionsUiEvent.Refresh) },
                modifier = Modifier.align(Alignment.Center),
            )
            state.error != null -> ErrorState(
                message = state.error,
                onRetry = { onEvent(PrescriptionsUiEvent.Refresh) },
                modifier = Modifier.align(Alignment.Center),
            )
            state.prescriptions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No prescriptions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.prescriptions) { prescription ->
                    PrescriptionCard(
                        prescription = prescription,
                        reminderEnabled = prescription.id in state.reminderEnabledIds,
                        reminderInProgress = state.reminderActionInProgressId == prescription.id,
                        onClick = { onEvent(PrescriptionsUiEvent.RequestViewPdf(prescription.id)) },
                        onReminderChange = { enabled ->
                            onEvent(PrescriptionsUiEvent.ToggleReminder(prescription, enabled))
                        },
                    )
                }
            }
        }
    }

    // PDF download confirmation dialog
    if (state.showPdfConfirm) {
        AlertDialog(
            onDismissRequest = { onEvent(PrescriptionsUiEvent.DismissPdfConfirm) },
            title = { Text("Open prescription PDF?") },
            text = { Text("This will download the document to view it in the app.") },
            confirmButton = {
                Button(onClick = { onEvent(PrescriptionsUiEvent.ConfirmViewPdf) }) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(PrescriptionsUiEvent.DismissPdfConfirm) }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun PrescriptionCard(
    prescription: Prescription,
    reminderEnabled: Boolean,
    reminderInProgress: Boolean,
    onClick: () -> Unit,
    onReminderChange: (Boolean) -> Unit,
) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = prescription.serviceName ?: "Prescription",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!prescription.doctorName.isNullOrBlank()) {
                        Text(
                            text = prescription.doctorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = DateUtil.toDisplayDate(prescription.visitDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(onClick = onClick) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("PDF")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExpandableInstructions(instructions = prescription.instructions)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = "${prescription.drugs.size} medication${if (prescription.drugs.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = onReminderChange,
                            enabled = !reminderInProgress && prescription.drugs.isNotEmpty(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableInstructions(instructions: String?) {
    if (instructions.isNullOrBlank()) return

    var expanded by remember(instructions) { mutableStateOf(false) }
    var hasOverflow by remember(instructions) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = instructions,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> hasOverflow = result.hasVisualOverflow },
        )
        if (hasOverflow || expanded) {
            Text(
                text = if (expanded) "Show less" else "Show more",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}
