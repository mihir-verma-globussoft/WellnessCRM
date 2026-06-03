package com.globussoft.wellness.patient.feature.wallet.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalletDto(
    val balance: Long,
    val currency: String,
    val transactions: List<WalletTransactionDto>,
)

@JsonClass(generateAdapter = true)
data class WalletTransactionDto(
    val id: Int,
    val type: String,
    val amount: Long,
    val description: String?,
    val date: String,
)

@JsonClass(generateAdapter = true)
data class GiftCardDto(
    val id: Int,
    val name: String,
    val amount: Long,
    val price: Long,
    val color: String?,
    val validityDays: Int,
    val currency: String,
    val expiresAt: String?,
)

@JsonClass(generateAdapter = true)
data class GiftCardStorefrontResponseDto(
    val giftCards: List<GiftCardDto>,
)

@JsonClass(generateAdapter = true)
data class GiftCardOrderDto(
    val patientId: Int?,
)

@JsonClass(generateAdapter = true)
data class GiftCardOrderResponseDto(
    val orderId: String,
    val paymentId: String,
    val key: String,
    val amount: Long,
    val currency: String,
    val giftCardId: Int,
    val patientId: Int,
    val patientName: String,
)

@JsonClass(generateAdapter = true)
data class GiftCardConfirmDto(
    val paymentId: String,
    val razorpay_order_id: String,
    val razorpay_payment_id: String,
    val razorpay_signature: String,
)

@JsonClass(generateAdapter = true)
data class GiftCardConfirmResponseDto(
    val giftCard: GiftCardDto,
    val transaction: WalletTransactionDto,
)

@JsonClass(generateAdapter = true)
data class FcmTokenDto(
    val token: String,
    val platform: String,
)
