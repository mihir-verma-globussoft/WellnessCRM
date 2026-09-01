package com.crm.enhance_wellness.feature.catalog.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.catalog.domain.model.ServicePackage
import com.crm.enhance_wellness.feature.catalog.domain.repository.CatalogRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetPackagesUseCase @Inject constructor(
    private val repository: CatalogRepository,
) {
    suspend operator fun invoke(): Result<List<ServicePackage>> = try {
        Result.Success(repository.getPackages())
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
