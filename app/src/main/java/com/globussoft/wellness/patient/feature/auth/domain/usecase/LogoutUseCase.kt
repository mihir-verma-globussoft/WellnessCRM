package com.globussoft.wellness.patient.feature.auth.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = try {
        repository.logout()
        Result.Success(Unit)
    } catch (e: IOException) {
        Result.Error("STORAGE_ERROR", "Failed to clear session data")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
