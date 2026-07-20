package com.crm.enhance_wellness.feature.wallet.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.wallet.domain.model.GiftCard
import com.crm.enhance_wellness.feature.wallet.domain.repository.GiftCardRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ConfirmGiftCardPurchaseUseCase @Inject constructor(
    private val repository: GiftCardRepository,
) {
    suspend operator fun invoke(
        giftCardId: Int,
        paymentId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
    ): Result<GiftCard> = try {
        Result.Success(
            repository.confirmOrder(
                giftCardId = giftCardId,
                paymentId = paymentId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature,
            )
        )
    } catch (e: HttpException) {
        Result.Error("HTTP_${e.code()}", e.message() ?: "Payment verification failed", e.code())
    } catch (e: IOException) {
        Result.Error("NETWORK_ERROR", "No internet connection. Please try again.")
    } catch (e: Exception) {
        Result.Error("UNEXPECTED_ERROR", e.message ?: "An unexpected error occurred")
    }
}
