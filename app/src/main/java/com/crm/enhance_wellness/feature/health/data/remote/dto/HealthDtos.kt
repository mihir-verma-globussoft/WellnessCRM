package com.crm.enhance_wellness.feature.health.data.remote.dto

import com.squareup.moshi.JsonClass

// GET /portal/prescriptions — `drugs` may be a JSON-encoded string or a real JSON array
// depending on the backend environment. @FlexibleDrugs normalises both to a JSON string.
// visit/doctor are nested objects returned by the API.
@JsonClass(generateAdapter = true)
data class PrescriptionDto(
    val id: Int,
    val visitId: Int?,
    @field:FlexibleDrugs val drugs: String?,
    val instructions: String?,
    val pdfUrl: String?,
    val visit: PrescriptionVisitDto?,
    val doctor: PrescriptionDoctorDto?,
    val createdAt: String?,
)

// GET /api/wellness/my-prescriptions — richer prescription payload used only for the
// "Request Meds" refresh; `validUntil` here is the backend's own computed expiry date, so
// depletion reminders can use it directly instead of estimating from duration/qty. Other
// fields in the real response (drugId, dosage, strengthValue, qty, ...) are inconsistently
// typed across drugs (e.g. drugId is sometimes an Int, sometimes ""), so this DTO only
// declares the two fields the reminder feature actually needs — Moshi ignores the rest.
@JsonClass(generateAdapter = true)
data class MyPrescriptionsResponseDto(
    val prescriptions: List<MyPrescriptionDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class MyPrescriptionDto(
    val id: Int,
    val validUntil: String? = null,
)

@JsonClass(generateAdapter = true)
data class PrescriptionVisitDto(
    val id: Int,
    val visitDate: String?,
    val service: PrescriptionServiceDto?,
)

@JsonClass(generateAdapter = true)
data class PrescriptionServiceDto(
    val name: String,
)

@JsonClass(generateAdapter = true)
data class PrescriptionDoctorDto(
    val id: Int,
    val name: String?,
)

data class DrugDto(
    val name: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val instructions: String?,
)

// GET /wellness/portal/prescription-requests — CUSTOMER JWT (verifyPatientToken); the
// backend filters by the authenticated patient, so no patientId is ever sent by the client.
// `requestedDrugs` (partial-refill requests) carries string dosage/frequency/duration;
// `prescription.drugs` (full-prescription requests) carries numeric fields — two different
// shapes for "a drug" in the same response, so they get separate DTOs. Both stay minimal
// (name + the display fields this screen actually needs) since fields like drugId/qty are
// inconsistently typed across entries in this backend (see MyPrescriptionsResponseDto above).
@JsonClass(generateAdapter = true)
data class PrescriptionRequestDto(
    val id: Int,
    val status: String,
    val prescriptionId: Int,
    val doctorName: String?,
    val isFullPrescription: Boolean,
    val requestedDrugs: List<RequestedDrugDto>?,
    val requestedDurationDays: Int?,
    val notes: String?,
    val reviewedByName: String?,
    val reviewedAt: String?,
    val reviewNote: String?,
    val createdAt: String?,
    val prescription: PrescriptionRequestPrescriptionDto?,
)

@JsonClass(generateAdapter = true)
data class RequestedDrugDto(
    val name: String,
)

@JsonClass(generateAdapter = true)
data class PrescriptionRequestPrescriptionDto(
    val id: Int,
    val drugs: List<PrescriptionRequestDrugDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class PrescriptionRequestDrugDto(
    val name: String,
)

// POST body for /api/wellness/portal/prescription-requests — reverse-engineered from the
// live web portal's PrescriptionRenewalComposer bundle (crm.globusdemos.com), since this is
// undocumented. `medicines` is omitted entirely (not sent as null/[]) for a full-prescription
// request — Moshi's default JsonWriter already skips null fields, matching that behavior.
@JsonClass(generateAdapter = true)
data class SubmitPrescriptionRequestDto(
    val prescriptionId: Int,
    val medicines: List<String>? = null,
    val durationDays: Int? = null,
    val notes: String? = null,
)

// GET /api/wellness/patients/{patientId}/treatment-plans — CUSTOMER JWT (verifyToken).
// Real shape confirmed against staging 2026-06-04.
@JsonClass(generateAdapter = true)
data class TreatmentPlanDto(
    val id: Int,
    val name: String,
    val totalSessions: Int,
    val completedSessions: Int,
    val startedAt: String,
    val nextDueAt: String?,
    val status: String,
    val totalPrice: Double,
    val patientId: Int,
    val serviceId: Int,
    val tenantId: Int,
    val patient: TreatmentPatientRefDto?,
    val service: TreatmentServiceRefDto?,
)

@JsonClass(generateAdapter = true)
data class TreatmentPatientRefDto(
    val id: Int,
    val name: String,
    val phone: String?,
)

@JsonClass(generateAdapter = true)
data class TreatmentServiceRefDto(
    val id: Int,
    val name: String,
    val category: String?,
)

// GET /api/wellness/patients/{patientId}/consents — CUSTOMER JWT (verifyToken).
// Real shape confirmed against staging 2026-06-04.
@JsonClass(generateAdapter = true)
data class ConsentFormDto(
    val id: Int,
    val templateName: String,
    val signedAt: String,
    val patientId: Int,
    val serviceId: Int,
    val hasPdfBlob: Boolean,
    val patient: ConsentPatientRefDto?,
    val service: ConsentServiceRefDto?,
)

@JsonClass(generateAdapter = true)
data class ConsentPatientRefDto(
    val id: Int,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class ConsentServiceRefDto(
    val id: Int,
    val name: String,
)
