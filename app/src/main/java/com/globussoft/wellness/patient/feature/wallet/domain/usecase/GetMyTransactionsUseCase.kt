package com.globussoft.wellness.patient.feature.wallet.domain.usecase

import com.globussoft.wellness.patient.core.storage.EncryptedPrefsManager
import com.globussoft.wellness.patient.core.util.Result
import com.globussoft.wellness.patient.feature.wallet.domain.model.WalletSummary
import com.globussoft.wellness.patient.feature.wallet.domain.repository.WalletRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetMyTransactionsUseCase @Inject constructor(
    private val repository: WalletRepository,
    private val encryptedPrefs: EncryptedPrefsManager,
) {
    suspend operator fun invoke(): Result<WalletSummary> = try {
        val patientId = encryptedPrefs.getPatientId()
        val summary = if (patientId != null) {
            repository.getWalletSummary(patientId)
        } else {
            repository.getMyTransactions()
        }
        Result.Success(summary)
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Server error", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    }
}
