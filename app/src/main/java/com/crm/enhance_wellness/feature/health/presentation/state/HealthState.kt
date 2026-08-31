package com.crm.enhance_wellness.feature.health.presentation.state

import com.crm.enhance_wellness.feature.health.domain.model.ConsentForm
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest
import com.crm.enhance_wellness.feature.health.domain.model.TreatmentPlan

data class PrescriptionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val prescriptions: List<Prescription> = emptyList(),
    val reminderEnabledIds: Set<Int> = emptySet(),
    val reminderActionInProgressId: Int? = null,
    val reminderMessage: String? = null,
    val exactAlarmPermissionPromptNeeded: Boolean = false,
    val permissionBlocked: Boolean = false,
    val showPdfConfirm: Boolean = false,
    val prescriptionToOpen: Int? = null,
    // Prescriptions with a PENDING/ACCEPTED renewal request already open — gates the
    // "Request a renewal" action per prescription card (shows "Renewal pending" instead).
    val openRenewalPrescriptionIds: Set<Int> = emptySet(),
    // True while openRenewalPrescriptionIds is being (re)fetched — there is no push channel
    // for renewal status, so the screen polls this on every refresh/resume; the pull-to-refresh
    // spinner must stay up until this settles too, not just the prescriptions list load.
    val renewalStatusLoading: Boolean = false,
    val renewalDialogPrescription: Prescription? = null,
    val renewalSubmitInProgress: Boolean = false,
    val renewalMessage: String? = null,
)

sealed class PrescriptionsUiEvent {
    object Refresh : PrescriptionsUiEvent()
    data class RequestViewPdf(val prescriptionId: Int) : PrescriptionsUiEvent()
    object ConfirmViewPdf : PrescriptionsUiEvent()
    object DismissPdfConfirm : PrescriptionsUiEvent()
    data class ViewPdf(val prescriptionId: Int) : PrescriptionsUiEvent()
    data class StartTreatmentAnalysis(val prescriptionId: Int) : PrescriptionsUiEvent()
    data class ToggleReminder(val prescription: Prescription, val enabled: Boolean) : PrescriptionsUiEvent()
    data class RequestRenewal(val prescription: Prescription) : PrescriptionsUiEvent()
    object DismissRenewalDialog : PrescriptionsUiEvent()
    data class SubmitRenewal(
        val fullPrescription: Boolean,
        val selectedMedicines: Set<String>,
        val durationDays: Int?,
        val notes: String?,
    ) : PrescriptionsUiEvent()
    object DismissRenewalMessage : PrescriptionsUiEvent()
    object DismissReminderMessage : PrescriptionsUiEvent()
    object ExactAlarmPermissionPromptShown : PrescriptionsUiEvent()
    object NavigateBack : PrescriptionsUiEvent()
    object ViewRequestHistory : PrescriptionsUiEvent()
}

data class PrescriptionPdfUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val pdfBytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrescriptionPdfUiState) return false
        return isLoading == other.isLoading && error == other.error && pdfBytes.contentEquals(other.pdfBytes)
    }
    override fun hashCode(): Int = 31 * isLoading.hashCode() + (pdfBytes?.contentHashCode() ?: 0)
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
    if (this == null && other == null) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}

sealed class PrescriptionPdfUiEvent {
    object NavigateBack : PrescriptionPdfUiEvent()
}

// ── Treatment Plans ───────────────────────────────────────────────────────────

data class TreatmentPlansUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val plans: List<TreatmentPlan> = emptyList(),
)

sealed class TreatmentPlansUiEvent {
    object Refresh : TreatmentPlansUiEvent()
    object NavigateBack : TreatmentPlansUiEvent()
}

// ── Consent Forms ─────────────────────────────────────────────────────────────

data class ConsentFormsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val forms: List<ConsentForm> = emptyList(),
)

sealed class ConsentFormsUiEvent {
    object Refresh : ConsentFormsUiEvent()
    data class ViewPdf(val consentId: Int) : ConsentFormsUiEvent()
    object NavigateBack : ConsentFormsUiEvent()
}

data class ConsentFormPdfUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val pdfBytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConsentFormPdfUiState) return false
        return isLoading == other.isLoading && error == other.error &&
            pdfBytes.contentEquals(other.pdfBytes)
    }
    override fun hashCode(): Int = 31 * isLoading.hashCode() + (pdfBytes?.contentHashCode() ?: 0)
}

sealed class ConsentFormPdfUiEvent {
    object NavigateBack : ConsentFormPdfUiEvent()
}

// ── Prescription Requests ("My Products / Medicines") ──────────────────────────

data class PrescriptionRequestsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val requests: List<PrescriptionRequest> = emptyList(),
)

sealed class PrescriptionRequestsUiEvent {
    object Refresh : PrescriptionRequestsUiEvent()
    object NavigateBack : PrescriptionRequestsUiEvent()
}
