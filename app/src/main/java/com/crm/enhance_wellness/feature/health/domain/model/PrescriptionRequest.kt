package com.crm.enhance_wellness.feature.health.domain.model

data class PrescriptionRequest(
    val id: Int,
    val status: String,
    val prescriptionId: Int,
    val doctorName: String?,
    val isFullPrescription: Boolean,
    val requestedDrugNames: List<String>,
    val prescriptionDrugNames: List<String>,
    val requestedDurationDays: Int?,
    val notes: String?,
    val reviewedByName: String?,
    val reviewedAt: String?,
    val reviewNote: String?,
    val createdAt: String?,
) {
    // A partial-refill request names its own drugs; a full-prescription request repeats the
    // whole original prescription, so fall back to that when the patient didn't request
    // specific drugs.
    val drugNames: List<String>
        get() = requestedDrugNames.ifEmpty { prescriptionDrugNames }

    // Matches the web portal's own open-request set (PENDING/ACCEPTED) — used to gate the
    // "Request a renewal" action per prescription and show "Renewal pending" instead.
    val isOpen: Boolean
        get() = status.uppercase() in OPEN_STATUSES
}

private val OPEN_STATUSES = setOf("PENDING", "ACCEPTED")

fun String.toRefillStatusLabel(): String = when (uppercase()) {
    "PENDING" -> "Pending"
    "ACCEPTED" -> "Approved"
    "REJECTED" -> "Rejected"
    "COMPLETED" -> "Completed"
    else -> replaceFirstChar { it.uppercase() }
}
