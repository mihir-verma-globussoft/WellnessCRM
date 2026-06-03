package com.globussoft.wellness.patient.feature.auth.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.auth.domain.model.Patient
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<Patient> = try {
        Result.Success(repository.login(email, password))
    } catch (e: HttpException) {
        when (e.code()) {
            400 -> Result.Error("INVALID_INPUT", "Email and password are required", 400)
            401 -> Result.Error("INVALID_CREDENTIALS", "Invalid email or password", 401)
            else -> Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
