package com.crm.enhance_wellness.feature.auth.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = try {
        Result.Success(repository.hasValidToken())
    } catch (e: IOException) {
        Result.Error("STORAGE_ERROR", "Failed to read auth state")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
