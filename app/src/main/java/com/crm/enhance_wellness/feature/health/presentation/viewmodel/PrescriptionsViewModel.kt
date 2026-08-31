package com.crm.enhance_wellness.feature.health.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.model.PatientPermissions
import com.crm.enhance_wellness.feature.auth.domain.usecase.GetPatientPermissionsUseCase
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionRequestsUseCase
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionsUseCase
import com.crm.enhance_wellness.feature.health.domain.usecase.SubmitPrescriptionRenewalUseCase
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiEvent
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiState
import com.crm.enhance_wellness.feature.health.reminder.PrescriptionReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PrescriptionsNavEvent {
    data class ToPdf(val prescriptionId: Int) : PrescriptionsNavEvent()
    data class ToAnalysis(val prescriptionId: Int, val visitId: Int?) : PrescriptionsNavEvent()
    object Back : PrescriptionsNavEvent()
    object ToRequestHistory : PrescriptionsNavEvent()
}

@HiltViewModel
class PrescriptionsViewModel @Inject constructor(
    private val getPrescriptions: GetPrescriptionsUseCase,
    private val getPermissions: GetPatientPermissionsUseCase,
    private val reminderRepository: PrescriptionReminderRepository,
    private val getPrescriptionRequests: GetPrescriptionRequestsUseCase,
    private val submitPrescriptionRenewal: SubmitPrescriptionRenewalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState())
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    private val _navEvent = Channel<PrescriptionsNavEvent>(Channel.BUFFERED)
    val navEvent = _navEvent.receiveAsFlow()

    init {
        observeReminderState()
        load()
        loadOpenRenewalRequests()
    }

    fun onEvent(event: PrescriptionsUiEvent) {
        when (event) {
            PrescriptionsUiEvent.Refresh -> { load(); loadOpenRenewalRequests() }
            is PrescriptionsUiEvent.RequestViewPdf ->
                _uiState.value = _uiState.value.copy(showPdfConfirm = true, prescriptionToOpen = event.prescriptionId)
            PrescriptionsUiEvent.ConfirmViewPdf -> {
                val id = _uiState.value.prescriptionToOpen ?: return
                _uiState.value = _uiState.value.copy(showPdfConfirm = false, prescriptionToOpen = null)
                viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.ToPdf(id)) }
            }
            PrescriptionsUiEvent.DismissPdfConfirm ->
                _uiState.value = _uiState.value.copy(showPdfConfirm = false, prescriptionToOpen = null)
            is PrescriptionsUiEvent.ViewPdf -> viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.ToPdf(event.prescriptionId)) }
            is PrescriptionsUiEvent.StartTreatmentAnalysis -> openTreatmentAnalysis(event.prescriptionId)
            is PrescriptionsUiEvent.ToggleReminder -> toggleReminder(event.prescription, event.enabled)
            is PrescriptionsUiEvent.RequestRenewal ->
                _uiState.value = _uiState.value.copy(renewalDialogPrescription = event.prescription)
            PrescriptionsUiEvent.DismissRenewalDialog ->
                _uiState.value = _uiState.value.copy(renewalDialogPrescription = null)
            is PrescriptionsUiEvent.SubmitRenewal ->
                submitRenewal(event.fullPrescription, event.selectedMedicines, event.durationDays, event.notes)
            PrescriptionsUiEvent.DismissRenewalMessage ->
                _uiState.value = _uiState.value.copy(renewalMessage = null)
            PrescriptionsUiEvent.DismissReminderMessage ->
                _uiState.value = _uiState.value.copy(reminderMessage = null)
            PrescriptionsUiEvent.ExactAlarmPermissionPromptShown ->
                _uiState.value = _uiState.value.copy(exactAlarmPermissionPromptNeeded = false)
            PrescriptionsUiEvent.NavigateBack -> viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.Back) }
            PrescriptionsUiEvent.ViewRequestHistory ->
                viewModelScope.launch { _navEvent.send(PrescriptionsNavEvent.ToRequestHistory) }
        }
    }

    private fun observeReminderState() {
        viewModelScope.launch {
            reminderRepository.cleanupExpired()
            reminderRepository.enabledReminderIds().collect { enabledIds ->
                _uiState.value = _uiState.value.copy(reminderEnabledIds = enabledIds)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, permissionBlocked = false)
            val permResult = getPermissions()
            if (permResult is Result.Success && !permResult.data.has(PatientPermissions.PRESCRIPTIONS_READ)) {
                _uiState.value = _uiState.value.copy(isLoading = false, permissionBlocked = true)
                return@launch
            }
            when (val result = getPrescriptions()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    prescriptions = result.data,
                    error = null,
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun toggleReminder(prescription: com.crm.enhance_wellness.feature.health.domain.model.Prescription, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                reminderActionInProgressId = prescription.id,
                reminderMessage = null,
            )
            val result = if (enabled) {
                reminderRepository.enable(prescription)
            } else {
                reminderRepository.disable(prescription.id)
            }
            _uiState.value = _uiState.value.copy(
                reminderActionInProgressId = null,
                reminderMessage = result.message,
                exactAlarmPermissionPromptNeeded = result.exactAlarmPermissionRequired,
            )
        }
    }

    private fun loadOpenRenewalRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(renewalStatusLoading = true)
            when (val result = getPrescriptionRequests()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    openRenewalPrescriptionIds = result.data.filter { it.isOpen }.map { it.prescriptionId }.toSet(),
                    renewalStatusLoading = false,
                )
                else -> _uiState.value = _uiState.value.copy(renewalStatusLoading = false)
                // Non-critical for this screen on failure; "My Products / Medicines" surfaces load errors.
                // renewalStatusLoading still clears so the refresh spinner doesn't hang, and the
                // previous (still possibly stale) ids are left as-is rather than wiped.
            }
        }
    }

    private fun submitRenewal(
        fullPrescription: Boolean,
        selectedMedicines: Set<String>,
        durationDays: Int?,
        notes: String?,
    ) {
        val prescription = _uiState.value.renewalDialogPrescription ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(renewalSubmitInProgress = true)
            val result = submitPrescriptionRenewal(
                prescriptionId = prescription.id,
                medicines = if (fullPrescription) null else selectedMedicines.toList(),
                durationDays = durationDays,
                notes = notes,
            )
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        renewalSubmitInProgress = false,
                        renewalDialogPrescription = null,
                        renewalMessage = "Renewal request sent.",
                    )
                    loadOpenRenewalRequests()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    renewalSubmitInProgress = false,
                    renewalMessage = result.message,
                )
                Result.Loading -> Unit
            }
        }
    }

    private fun openTreatmentAnalysis(prescriptionId: Int) {
        val visitId = _uiState.value.prescriptions.firstOrNull { it.id == prescriptionId }?.visitId
        viewModelScope.launch {
            _navEvent.send(PrescriptionsNavEvent.ToAnalysis(prescriptionId, visitId))
        }
    }
}
