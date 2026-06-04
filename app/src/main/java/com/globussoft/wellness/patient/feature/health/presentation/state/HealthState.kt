package com.globussoft.wellness.patient.feature.health.presentation.state

import com.globussoft.wellness.patient.feature.health.domain.model.Prescription

data class PrescriptionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val prescriptions: List<Prescription> = emptyList(),
)

sealed class PrescriptionsUiEvent {
    object Refresh : PrescriptionsUiEvent()
    data class ViewPdf(val prescriptionId: Int) : PrescriptionsUiEvent()
    object NavigateBack : PrescriptionsUiEvent()
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
