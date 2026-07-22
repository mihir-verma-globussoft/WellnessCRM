package com.crm.enhance_wellness.feature.health.data.mapper

import com.crm.enhance_wellness.core.util.DateUtil
import com.crm.enhance_wellness.feature.health.data.local.entity.CachedPrescription
import com.crm.enhance_wellness.feature.health.data.remote.dto.ConsentFormDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.DrugDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.TreatmentPlanDto
import com.crm.enhance_wellness.feature.health.domain.model.ConsentForm
import com.crm.enhance_wellness.feature.health.domain.model.Drug
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.model.TreatmentPlan
import org.json.JSONArray
import org.json.JSONObject

fun PrescriptionDto.toDomain() = Prescription(
    id = id,
    visitId = visitId,
    visitDate = visit?.visitDate,
    createdAt = createdAt,
    instructions = instructions,
    doctorName = doctor?.name,
    serviceName = visit?.service?.name,
    drugs = parseDrugsJson(drugs),
)

internal fun parseDrugsJson(json: String): List<Drug> = runCatching {
    val trimmed = json.trim()
    when {
        trimmed.startsWith("[") -> JSONArray(trimmed).toDrugList()
        trimmed.startsWith("{") -> JSONObject(trimmed).toDrugList()
        else -> emptyList()
    }
}.getOrDefault(emptyList())

private fun JSONObject.toDrugList(): List<Drug> {
    val wrappedArray = optJSONArray("drugs")
        ?: optJSONArray("medications")
        ?: optJSONArray("items")
        ?: optJSONArray("data")
    return wrappedArray?.toDrugList() ?: listOfNotNull(toDrugOrNull())
}

private fun JSONArray.toDrugList(): List<Drug> =
    (0 until length()).mapNotNull { index ->
        optJSONObject(index)?.toDrugOrNull()
    }

private fun JSONObject.toDrugOrNull(): Drug? {
    val nestedDrug = optJSONObject("drug")
        ?: optJSONObject("medicine")
        ?: optJSONObject("medication")
        ?: optJSONObject("product")
    val name = firstString(
        "name",
        "drugName",
        "medicineName",
        "medicationName",
        "title",
        "label",
    ) ?: nestedDrug?.firstString("name", "drugName", "medicineName", "title")

    val frequency = firstString(
        "frequency",
        "frequencyPerDay",
        "dailyFrequency",
        "timesPerDay",
        "times",
        "perDay",
        "noOfTimes",
        "numberOfTimes",
    )
    val duration = firstString(
        "duration",
        "durationDays",
        "days",
        "noOfDays",
        "numberOfDays",
        "courseDuration",
    )

    if (name.isNullOrBlank()) return null
    return Drug(
        name = name,
        dosage = firstString("dosage", "dose", "dosageValue", "quantity", "strength"),
        frequency = frequency,
        duration = duration,
        instructions = firstString("instructions", "instruction", "notes", "remarks"),
    )
}

private fun JSONObject.firstString(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        if (!has(key) || isNull(key)) return@firstNotNullOfOrNull null
        opt(key)?.toString()?.trim()?.takeUnless { value ->
            value.isBlank() || value.equals("null", ignoreCase = true)
        }
    }

fun CachedPrescription.toDomain() = Prescription(
    id = id,
    visitId = visitId,
    visitDate = if (visitDate > 0L) DateUtil.epochMsToIso(visitDate) else null,
    createdAt = if (visitDate > 0L) DateUtil.epochMsToIso(visitDate) else null,
    instructions = null,
    doctorName = doctorName,
    serviceName = serviceName,
    drugs = emptyList(),
    pdfBytes = pdfBytes,
    pdfCachedAt = pdfCachedAt,
)

fun Prescription.toEntity() = CachedPrescription(
    id = id,
    visitId = visitId,
    visitDate = DateUtil.isoToEpochMs(visitDate),
    doctorName = doctorName,
    serviceName = serviceName,
    drugCount = drugs.size,
    pdfBytes = pdfBytes,
    pdfCachedAt = pdfCachedAt,
    cachedAt = System.currentTimeMillis(),
)

fun TreatmentPlanDto.toDomain() = TreatmentPlan(
    id = id,
    name = name,
    totalSessions = totalSessions,
    completedSessions = completedSessions,
    startedAt = startedAt,
    nextDueAt = nextDueAt,
    status = status,
    totalPrice = totalPrice,
    serviceName = service?.name,
    serviceCategory = service?.category,
)

fun ConsentFormDto.toDomain() = ConsentForm(
    id = id,
    templateName = templateName,
    signedAt = signedAt,
    hasPdfBlob = hasPdfBlob,
    serviceName = service?.name,
)
