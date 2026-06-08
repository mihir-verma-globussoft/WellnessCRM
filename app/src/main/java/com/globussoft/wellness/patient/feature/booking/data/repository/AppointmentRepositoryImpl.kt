package com.globussoft.wellness.patient.feature.booking.data.repository

import com.globussoft.wellness.patient.core.network.WellnessApiService
import com.globussoft.wellness.patient.feature.booking.data.local.dao.VisitDao
import com.globussoft.wellness.patient.feature.booking.data.mapper.toDomain
import com.globussoft.wellness.patient.feature.booking.data.mapper.toEntity
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.AddWaitlistDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.BookAppointmentDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.RescheduleAppointmentDto
import com.globussoft.wellness.patient.feature.booking.domain.model.Appointment
import com.globussoft.wellness.patient.feature.booking.domain.model.Product
import com.globussoft.wellness.patient.feature.booking.domain.model.ProductCategory
import com.globussoft.wellness.patient.feature.booking.domain.model.Visit
import com.globussoft.wellness.patient.feature.booking.domain.model.WaitlistEntry
import com.globussoft.wellness.patient.feature.booking.domain.repository.AppointmentRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: WellnessApiService,
    private val visitDao: VisitDao,
) : AppointmentRepository {

    override suspend fun getMyAppointments(bucket: String?): List<Appointment> {
        val response = api.getMyAppointments(bucket)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.appointments.map { it.toDomain() }
    }

    override suspend fun bookAppointment(
        appointmentDate: String,
        appointmentTime: String,
        reason: String,
        serviceId: Int?,
        membershipId: Int?,
        bookingType: String?,
    ): Appointment {
        val response = api.bookAppointment(
            BookAppointmentDto(
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                reason = reason,
                serviceId = serviceId,
                membershipId = membershipId,
                bookingType = bookingType,
                doctorId = null,
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.appointment.toDomain()
    }

    override suspend fun cancelAppointment(appointmentId: Int): Appointment {
        val response = api.cancelAppointment(appointmentId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.appointment.toDomain()
    }

    override suspend fun rescheduleAppointment(
        appointmentId: Int,
        appointmentDate: String,
        appointmentTime: String,
    ): Appointment {
        val response = api.rescheduleAppointment(
            appointmentId,
            RescheduleAppointmentDto(appointmentDate = appointmentDate, appointmentTime = appointmentTime),
        )
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.appointment.toDomain()
    }

    override suspend fun getPortalProducts(): List<Product> {
        val response = api.getPortalProducts()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.map { it.toDomain() }
    }

    override suspend fun getPortalProductCategories(): List<ProductCategory> {
        val response = api.getPortalProductCategories()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.map { it.toDomain() }
    }

    override suspend fun getVisitHistory(upcomingOnly: Boolean): List<Visit> {
        val response = api.getVisits(upcoming = if (upcomingOnly) true else null)
        if (!response.isSuccessful) throw HttpException(response)
        val visits = response.body()!!.map { it.toDomain() }
        visitDao.insertAll(visits.map { it.toEntity() })
        return visits
    }

    override suspend fun getCachedVisits(): List<Visit> =
        visitDao.getAll().map { it.toDomain() }

    override suspend fun cacheVisits(visits: List<Visit>) {
        visitDao.insertAll(visits.map { it.toEntity() })
    }

    override suspend fun getWaitlist(): List<WaitlistEntry> {
        val response = api.getWaitlist()
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.map { it.toDomain() }
    }

    override suspend fun addToWaitlist(serviceId: Int, patientId: Int, notes: String?): WaitlistEntry {
        val response = api.addToWaitlist(AddWaitlistDto(serviceId = serviceId, patientId = patientId, notes = notes))
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()!!.toDomain()
    }
}
