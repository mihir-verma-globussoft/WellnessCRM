package com.crm.enhance_wellness.feature.auth.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.model.EmailAlreadyRegisteredException
import com.crm.enhance_wellness.feature.auth.domain.repository.AuthRepository
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SendRegisterOtpUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String): Result<Unit> = try {
        repository.sendRegisterOtp(email)
        Result.Success(Unit)
    } catch (e: EmailAlreadyRegisteredException) {
        Result.Error("ALREADY_REGISTERED", e.message!!, 409)
    } catch (e: HttpException) {
        val msg = runCatching { JSONObject(e.response()?.errorBody()?.string() ?: "").getString("error") }.getOrNull()
        Result.Error("HTTP_${e.code()}", msg ?: "Could not send verification code", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
