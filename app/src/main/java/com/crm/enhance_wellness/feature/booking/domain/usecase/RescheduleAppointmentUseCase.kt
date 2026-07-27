package com.crm.enhance_wellness.feature.booking.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.booking.domain.model.Appointment
import com.crm.enhance_wellness.feature.booking.domain.repository.AppointmentRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RescheduleAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        appointmentId: Int,
        appointmentDate: String,
        appointmentTime: String,
    ): Result<Appointment> = try {
        Result.Success(repository.rescheduleAppointment(appointmentId, appointmentDate, appointmentTime))
    } catch (e: HttpException) {
        val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: ""
        val backendMessage = runCatching { JSONObject(rawBody).getString("error") }.getOrNull()
        when (e.code()) {
            409 -> Result.Error("SLOT_TAKEN", backendMessage ?: "This slot is no longer available", 409)
            404 -> Result.Error("NOT_FOUND", backendMessage ?: "Appointment not found", 404)
            else -> Result.Error("HTTP_${e.code()}", backendMessage ?: e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
