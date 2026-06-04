package com.globussoft.wellness.patient.feature.profile.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.SectionLabel
import com.globussoft.wellness.patient.core.ui.WellnessCard
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
                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = { onEvent(ProfileUiEvent.Refresh) },
                    modifier = Modifier.align(Alignment.Center),
                )
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WellnessCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Personal Information")
                HorizontalDivider()
                ProfileField("Name", profile.name)
                if (!profile.phone.isNullOrBlank()) ProfileField("Phone", profile.phone)
                if (!profile.email.isNullOrBlank()) ProfileField("Email", profile.email)
                if (!profile.dob.isNullOrBlank()) ProfileField("Date of birth", DateUtil.toDisplayDate(profile.dob))
                if (!profile.gender.isNullOrBlank()) ProfileField(
                    "Gender",
                    when (profile.gender.uppercase()) {
                        "F" -> "Female"
                        "M" -> "Male"
                        else -> profile.gender
                    },
                )
            }
        }

        WellnessCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Data & Privacy")
                HorizontalDivider()
                if (state.exportRequested) {
                    Text(
                        "Export request submitted. You will receive your data by email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    OutlinedButton(
                        onClick = { onEvent(ProfileUiEvent.RequestDsarExport) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Text("Request data export")
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(ProfileUiEvent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Note: Phone, date of birth, and gender can only be updated at the clinic.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
        SectionLabel("Change password (optional)")
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
            Text(
                state.saveError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { onEvent(ProfileUiEvent.CancelEdit) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onEvent(ProfileUiEvent.SaveChanges) },
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    }
}
