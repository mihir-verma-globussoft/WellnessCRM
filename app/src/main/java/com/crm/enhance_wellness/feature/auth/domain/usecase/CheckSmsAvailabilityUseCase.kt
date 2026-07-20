package com.crm.enhance_wellness.feature.auth.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class CheckSmsAvailabilityUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = try {
        Result.Success(repository.isSmsAvailable())
    } catch (e: Exception) {
        Result.Success(true)
    }
}
