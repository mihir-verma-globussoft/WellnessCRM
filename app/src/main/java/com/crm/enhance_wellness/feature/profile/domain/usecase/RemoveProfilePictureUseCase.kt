package com.crm.enhance_wellness.feature.profile.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.profile.domain.model.Profile
import com.crm.enhance_wellness.feature.profile.domain.repository.ProfileRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RemoveProfilePictureUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<Profile> = try {
        Result.Success(repository.removeProfilePicture())
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Failed to remove photo", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
