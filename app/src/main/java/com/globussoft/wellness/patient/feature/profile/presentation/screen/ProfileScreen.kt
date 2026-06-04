package com.globussoft.wellness.patient.feature.profile.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.profile.presentation.state.ProfileUiEvent
import com.globussoft.wellness.patient.feature.profile.presentation.state.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ProfileUiEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.isEditing) {
                        IconButton(onClick = { onEvent(ProfileUiEvent.StartEdit) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                        }
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
                    Button(onClick = { onEvent(ProfileUiEvent.Refresh) }) { Text("Retry") }
                }
                state.isEditing -> EditProfileContent(state = state, onEvent = onEvent)
                else -> ViewProfileContent(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun ViewProfileContent(state: ProfileUiState, onEvent: (ProfileUiEvent) -> Unit) {
    val profile = state.profile ?: return
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Personal Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                HorizontalDivider()
                ProfileField("Name", profile.name)
                if (!profile.phone.isNullOrBlank()) ProfileField("Phone", profile.phone)
                if (!profile.email.isNullOrBlank()) ProfileField("Email", profile.email)
                if (!profile.dob.isNullOrBlank()) ProfileField("Date of birth", DateUtil.toDisplayDate(profile.dob))
                if (!profile.gender.isNullOrBlank()) ProfileField("Gender", when (profile.gender.uppercase()) {
                    "F" -> "Female"
                    "M" -> "Male"
                    else -> profile.gender
                })
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Data & Privacy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                HorizontalDivider()
                if (state.exportRequested) {
                    Text("Export request submitted. You will receive your data by email.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                } else {
                    OutlinedButton(onClick = { onEvent(ProfileUiEvent.RequestDsarExport) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Request data export")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(ProfileUiEvent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Log out")
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EditProfileContent(state: ProfileUiState, onEvent: (ProfileUiEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Note: Phone, date of birth, and gender can only be updated at the clinic.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = state.editName,
            onValueChange = { onEvent(ProfileUiEvent.EditName(it)) },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.editEmail,
            onValueChange = { onEvent(ProfileUiEvent.EditEmail(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        HorizontalDivider()
        Text("Change password (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = state.currentPassword,
            onValueChange = { onEvent(ProfileUiEvent.EditCurrentPassword(it)) },
            label = { Text("Current password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        OutlinedTextField(
            value = state.newPassword,
            onValueChange = { onEvent(ProfileUiEvent.EditNewPassword(it)) },
            label = { Text("New password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        if (state.saveError != null) {
            Text(state.saveError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onEvent(ProfileUiEvent.CancelEdit) }, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onEvent(ProfileUiEvent.SaveChanges) },
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    }
}
