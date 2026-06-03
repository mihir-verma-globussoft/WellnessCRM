package com.globussoft.wellness.patient.feature.auth.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class CheckAuthStatusUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = try {
        Result.Success(repository.hasValidToken())
    } catch (e: IOException) {
        Result.Error("STORAGE_ERROR", "Failed to read auth state")
    }
}
