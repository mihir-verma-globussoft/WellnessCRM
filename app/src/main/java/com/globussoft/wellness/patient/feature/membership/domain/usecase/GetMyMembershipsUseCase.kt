package com.globussoft.wellness.patient.feature.membership.domain.usecase

import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.membership.domain.model.Membership
import com.globussoft.wellness.patient.feature.membership.domain.repository.MembershipRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetMyMembershipsUseCase @Inject constructor(
    private val repository: MembershipRepository,
) {
    suspend operator fun invoke(): Result<List<Membership>> = try {
        Result.Success(repository.getMyMemberships())
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        val cached = repository.getCachedMemberships()
        if (cached.isNotEmpty()) Result.Success(cached)
        else Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
