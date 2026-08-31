package com.crm.enhance_wellness.feature.health.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRequestRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SubmitPrescriptionRenewalUseCase @Inject constructor(
    private val repository: PrescriptionRequestRepository,
) {
    suspend operator fun invoke(
        prescriptionId: Int,
        medicines: List<String>?,
        durationDays: Int?,
        notes: String?,
    ): Result<Unit> = try {
        repository.submitRenewalRequest(prescriptionId, medicines, durationDays, notes)
        Result.Success(Unit)
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Result.Error("UNAUTHORIZED", "Session expired. Please log in again.", 401)
            403 -> Result.Error("FORBIDDEN", "You don't have access to this. Contact your clinic.", 403)
            else -> Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
