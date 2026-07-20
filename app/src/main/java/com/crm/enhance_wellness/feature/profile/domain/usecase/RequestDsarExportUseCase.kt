package com.crm.enhance_wellness.feature.profile.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.profile.domain.repository.ProfileRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RequestDsarExportUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<Unit> = try {
        repository.requestDsarExport()
        Result.Success(Unit)
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
