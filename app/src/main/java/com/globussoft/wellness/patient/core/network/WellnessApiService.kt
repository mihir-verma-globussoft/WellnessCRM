package com.globussoft.wellness.patient.core.network

import com.globussoft.wellness.patient.feature.auth.data.remote.dto.OtpRequestDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.OtpVerifyDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.OtpVerifyResponseDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.RegisterPatientDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.RegisterResponseDto
import com.globussoft.wellness.patient.feature.auth.data.remote.dto.TenantBrandingDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.AppointmentDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.BookAppointmentDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.BookAppointmentResponseDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.CancelAppointmentResponseDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.LocationDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.ServiceDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.SlotDto
import com.globussoft.wellness.patient.feature.booking.data.remote.dto.VisitDto
import com.globussoft.wellness.patient.feature.health.data.remote.dto.PrescriptionDto
import com.globussoft.wellness.patient.feature.membership.data.remote.dto.MembershipDto
import com.globussoft.wellness.patient.feature.membership.data.remote.dto.MembershipPlanDto
import com.globussoft.wellness.patient.feature.profile.data.remote.dto.ProfileDto
import com.globussoft.wellness.patient.feature.profile.data.remote.dto.UpdateProfileDto
import com.globussoft.wellness.patient.feature.profile.data.remote.dto.DsarExportResponseDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.FcmTokenDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardOrderDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardOrderResponseDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardConfirmDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardConfirmResponseDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.GiftCardStorefrontResponseDto
import com.globussoft.wellness.patient.feature.wallet.data.remote.dto.WalletDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WellnessApiService {

    // ── Tenant Branding ──────────────────────────────────────────────────────
    @GET("public/tenant/{slug}")
    suspend fun getTenantBranding(
        @Path("slug") slug: String,
    ): Response<TenantBrandingDto>

    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("portal/login/request-otp")
    suspend fun requestOtp(
        @Body body: OtpRequestDto,
    ): Response<Map<String, Any>>

    @POST("portal/login/verify-otp")
    suspend fun verifyOtp(
        @Body body: OtpVerifyDto,
    ): Response<OtpVerifyResponseDto>

    @POST("portal/register")
    suspend fun registerPatient(
        @Body body: RegisterPatientDto,
    ): Response<RegisterResponseDto>

    // ── Profile ───────────────────────────────────────────────────────────────
    @GET("portal/me")
    suspend fun getProfile(): Response<ProfileDto>

    @PUT("portal/me")
    suspend fun updateProfile(
        @Body body: UpdateProfileDto,
    ): Response<ProfileDto>

    @POST("portal/export")
    suspend fun requestDsarExport(): Response<DsarExportResponseDto>

    // ── FCM Token ─────────────────────────────────────────────────────────────
    @POST("portal/me/fcm-token")
    suspend fun registerFcmToken(
        @Body body: FcmTokenDto,
    ): Response<Unit>

    @DELETE("portal/me/fcm-token")
    suspend fun deregisterFcmToken(): Response<Unit>

    // ── Visits / Appointments ─────────────────────────────────────────────────
    @GET("portal/visits")
    suspend fun getVisits(
        @Query("upcoming") upcoming: Boolean? = null,
    ): Response<List<VisitDto>>

    @GET("services")
    suspend fun getServices(): Response<List<ServiceDto>>

    @GET("locations")
    suspend fun getLocations(): Response<List<LocationDto>>

    @GET("portal/slots")
    suspend fun getAvailableSlots(
        @Query("serviceId") serviceId: Int,
        @Query("locationId") locationId: Int,
        @Query("date") date: String,
    ): Response<List<SlotDto>>

    @POST("appointments/book")
    suspend fun bookAppointment(
        @Body body: BookAppointmentDto,
    ): Response<BookAppointmentResponseDto>

    @GET("appointments/my")
    suspend fun getMyAppointments(): Response<List<AppointmentDto>>

    @POST("appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") appointmentId: Int,
    ): Response<CancelAppointmentResponseDto>

    // ── Prescriptions ─────────────────────────────────────────────────────────
    @GET("portal/prescriptions")
    suspend fun getPrescriptions(): Response<List<PrescriptionDto>>

    @GET("portal/prescriptions/{id}/pdf")
    suspend fun getPrescriptionPdf(
        @Path("id") prescriptionId: Int,
    ): Response<ResponseBody>

    // ── Memberships ───────────────────────────────────────────────────────────
    @GET("portal/me/memberships")
    suspend fun getMyMemberships(): Response<List<MembershipDto>>

    @GET("membership-plans")
    suspend fun getMembershipPlans(): Response<List<MembershipPlanDto>>

    // ── Wallet ────────────────────────────────────────────────────────────────
    @GET("portal/me/wallet")
    suspend fun getWallet(): Response<WalletDto>

    // ── Gift Cards ────────────────────────────────────────────────────────────
    @GET("giftcards/storefront")
    suspend fun getGiftCardStorefront(): Response<GiftCardStorefrontResponseDto>

    @POST("giftcards/{id}/purchase/order")
    suspend fun initiateGiftCardPurchase(
        @Path("id") giftCardId: Int,
        @Body body: GiftCardOrderDto,
    ): Response<GiftCardOrderResponseDto>

    @POST("giftcards/{id}/purchase/confirm")
    suspend fun confirmGiftCardPurchase(
        @Path("id") giftCardId: Int,
        @Body body: GiftCardConfirmDto,
    ): Response<GiftCardConfirmResponseDto>

    // ── Dashboard (backend gap — stub until endpoint is built) ────────────────
    @GET("portal/me/dashboard")
    suspend fun getDashboard(): Response<Map<String, Any>>
}
