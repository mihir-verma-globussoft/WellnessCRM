package com.crm.enhance_wellness.feature.health.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetPrescriptionsUseCase @Inject constructor(
    private val repository: PrescriptionRepository,
) {
    suspend operator fun invoke(): Result<List<Prescription>> = try {
        Result.Success(repository.getPrescriptions())
    } catch (e: HttpException) {
        val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: ""
        val backendMessage = runCatching { JSONObject(rawBody).getString("error") }.getOrNull()
        Result.Error("HTTP_${e.code()}", backendMessage ?: e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        val cached = repository.getCachedPrescriptions()
        if (cached.isNotEmpty()) Result.Success(cached)
        else Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
