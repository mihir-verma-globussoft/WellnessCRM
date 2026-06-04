package com.globussoft.wellness.patient.feature.booking.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.booking.domain.model.Appointment
import com.globussoft.wellness.patient.feature.booking.domain.repository.AppointmentRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BookAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        appointmentDate: String,
        appointmentTime: String,
        reason: String,
        serviceId: Int? = null,
        membershipId: Int? = null,
        bookingType: String? = null,
    ): Result<Appointment> = try {
        Result.Success(
            repository.bookAppointment(
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                reason = reason,
                serviceId = serviceId,
                membershipId = membershipId,
                bookingType = bookingType,
            )
        )
    } catch (e: HttpException) {
        when (e.code()) {
            400 -> Result.Error("MISSING_FIELDS", "Please fill in all required fields", 400)
            409 -> Result.Error("DOCTOR_UNAVAILABLE", "This slot is no longer available", 409)
            else -> Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
