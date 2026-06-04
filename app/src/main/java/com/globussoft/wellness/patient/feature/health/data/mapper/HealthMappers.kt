package com.globussoft.wellness.patient.feature.health.data.mapper

import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.health.data.local.entity.CachedPrescription
import com.globussoft.wellness.patient.feature.health.data.remote.dto.DrugDto
import com.globussoft.wellness.patient.feature.health.data.remote.dto.PrescriptionDto
import com.globussoft.wellness.patient.feature.health.domain.model.Drug
import com.globussoft.wellness.patient.feature.health.domain.model.Prescription

fun PrescriptionDto.toDomain() = Prescription(
    id = id,
    visitId = visitId,
    visitDate = visitDate,
    doctorName = doctorName,
    serviceName = serviceName,
    drugs = drugs.map { it.toDomain() },
)

fun DrugDto.toDomain() = Drug(
    name = name,
    dosage = dosage,
    frequency = frequency,
    duration = duration,
    instructions = instructions,
)

fun CachedPrescription.toDomain() = Prescription(
    id = id,
    visitId = visitId,
    visitDate = if (visitDate > 0L) DateUtil.epochMsToIso(visitDate) else null,
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
