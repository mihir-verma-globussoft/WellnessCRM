package com.globussoft.wellness.patient.feature.auth.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.auth.domain.model.Patient
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RegisterPatientUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Patient> = try {
        Result.Success(repository.register(email, password, name))
    } catch (e: HttpException) {
        when (e.code()) {
            400 -> {
                val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: ""
                if (body.contains("Email already registered", ignoreCase = true)) {
                    Result.Error("ALREADY_REGISTERED", "This email is already registered", 400)
                } else {
                    Result.Error("INVALID_INPUT", "Please check your details and try again", 400)
                }
            }
            else -> Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
