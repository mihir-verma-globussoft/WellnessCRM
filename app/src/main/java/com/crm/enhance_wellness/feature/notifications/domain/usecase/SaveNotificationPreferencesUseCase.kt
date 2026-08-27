package com.crm.enhance_wellness.feature.notifications.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.notifications.domain.model.NotificationPreferences
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationPreferencesRepository
import java.io.IOException
import javax.inject.Inject

class SaveNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationPreferencesRepository,
) {
    suspend operator fun invoke(preferences: NotificationPreferences): Result<Unit> = try {
        repository.save(preferences)
        Result.Success(Unit)
    } catch (e: IOException) {
        Result.Error("STORAGE_ERROR", "Couldn't save your settings. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
