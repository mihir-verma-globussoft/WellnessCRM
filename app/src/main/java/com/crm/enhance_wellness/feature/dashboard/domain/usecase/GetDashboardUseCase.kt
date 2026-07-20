package com.crm.enhance_wellness.feature.dashboard.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.dashboard.domain.model.Dashboard
import com.crm.enhance_wellness.feature.dashboard.domain.repository.DashboardRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(): Result<Dashboard> = try {
        Result.Success(repository.getDashboard())
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
