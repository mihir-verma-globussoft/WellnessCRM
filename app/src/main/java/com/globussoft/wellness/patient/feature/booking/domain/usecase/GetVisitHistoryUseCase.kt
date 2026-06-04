package com.globussoft.wellness.patient.feature.booking.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.booking.domain.model.Visit
import com.globussoft.wellness.patient.feature.booking.domain.repository.AppointmentRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetVisitHistoryUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(upcomingOnly: Boolean = false): Result<List<Visit>> = try {
        Result.Success(repository.getVisitHistory(upcomingOnly))
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        val cached = repository.getCachedVisits()
        if (cached.isNotEmpty()) Result.Success(cached)
        else Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
