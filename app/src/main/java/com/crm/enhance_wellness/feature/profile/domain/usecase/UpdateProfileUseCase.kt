package com.crm.enhance_wellness.feature.profile.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.profile.domain.model.Profile
import com.crm.enhance_wellness.feature.profile.domain.repository.ProfileRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(
        name: String? = null,
        email: String? = null,
        currentPassword: String? = null,
        newPassword: String? = null,
    ): Result<Profile> = try {
        Result.Success(repository.updateProfile(name, email, currentPassword, newPassword))
    } catch (e: HttpException) {
        val rawBody = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: ""
        val backendMessage = runCatching {
            JSONObject(rawBody).let { json ->
                json.optString("error").takeIf { it.isNotBlank() }
                    ?: json.optString("message").takeIf { it.isNotBlank() }
                    ?: json.optString("detail").takeIf { it.isNotBlank() }
            }
        }.getOrNull()
        when (e.code()) {
            400 -> Result.Error("INVALID_INPUT", backendMessage ?: "Invalid input — check your current password", 400)
            401 -> Result.Error("UNAUTHORIZED", backendMessage ?: "Current password is incorrect", 401)
            else -> Result.Error("HTTP_${e.code()}", backendMessage ?: e.message() ?: "Server error", e.code())
        }
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
