package com.globussoft.wellness.patient.feature.booking.data.mapper

import com.globussoft.wellness.patient.core.util.DateUtil
import com.globussoft.wellness.patient.feature.booking.data.local.entity.CachedVisit
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.AppointmentDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.ProductCategoryDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.ProductDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.VisitDto
import com.globussoft.wellness.patient.feature.booking.domain.model.Appointment
import com.globussoft.wellness.patient.feature.booking.domain.model.Product
import com.globussoft.wellness.patient.feature.booking.domain.model.ProductCategory
import com.globussoft.wellness.patient.feature.booking.domain.model.Visit

fun AppointmentDto.toDomain() = Appointment(
    id = id,
    doctorName = doctorName,
    serviceName = serviceName,
    appointmentDate = appointmentDate,
    status = status,
    reason = reason,
    doctorAssigned = doctorAssigned,
    bookingType = bookingType,
    videoCallUrl = videoCallUrl,
)

fun VisitDto.toDomain() = Visit(
    id = id,
    visitDate = visitDate,
    status = status,
    serviceName = service?.name,
    doctorName = doctor?.name,
    locationName = locationName,
    bookingType = bookingType,
    videoCallUrl = videoCallUrl,
    amountCharged = amountCharged,
)

fun CachedVisit.toDomain() = Visit(
    id = id,
    visitDate = DateUtil.epochMsToIso(visitDate),
    status = status,
    serviceName = serviceName,
    doctorName = doctorName,
    locationName = locationName,
    bookingType = bookingType,
    videoCallUrl = videoCallUrl,
    amountCharged = amountCharged,
)

fun Visit.toEntity() = CachedVisit(
    id = id,
    visitDate = DateUtil.isoToEpochMs(visitDate),
    status = status,
    serviceName = serviceName,
    doctorName = doctorName,
    locationName = locationName,
    bookingType = bookingType,
    videoCallUrl = videoCallUrl,
    amountCharged = amountCharged,
    cachedAt = System.currentTimeMillis(),
)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    description = description,
    price = price,
    discountedPrice = discountedPrice,
    imageUrl = imageUrl,
    categoryName = category?.name,
)

fun ProductCategoryDto.toDomain() = ProductCategory(
    id = id,
    name = name,
    imageUrl = imageUrl,
    color = color,
)
