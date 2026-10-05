package com.crm.enhance_wellness.core.network

import com.crm.enhance_wellness.feature.auth.data.remote.dto.CheckEmailRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.CheckEmailResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.EmailOtpRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.EmailOtpVerifyRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.EmailOtpVerifyResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.LoginRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.LoginResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.PatientPermissionsDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.PortalHealthDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.RegisterRequestDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.RegisterResponseDto
import com.crm.enhance_wellness.feature.auth.data.remote.dto.TenantBrandingResponseDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceCategoryDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.CatalogServiceDto
import com.crm.enhance_wellness.feature.catalog.data.remote.dto.ServicePackagesResponseDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.AddWaitlistDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.AppointmentListResponseDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.BookAppointmentDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.BookAppointmentResponseDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.CancelAppointmentResponseDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.ProductDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.RescheduleAppointmentDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.RescheduleAppointmentResponseDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.VisitDto
import com.crm.enhance_wellness.feature.booking.data.remote.dto.WaitlistEntryDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.ConsentFormDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.MyPrescriptionsResponseDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.SubmitPrescriptionRequestDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.TreatmentPlanDto
import com.crm.enhance_wellness.feature.loyalty.data.remote.dto.LoyaltyResponseDto
import com.crm.enhance_wellness.feature.membership.data.remote.dto.MembershipDto
import com.crm.enhance_wellness.feature.membership.data.remote.dto.MembershipPlanDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.AuthProfileResponseDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.DeleteAccountRequestDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.DeleteAccountResponseDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.DsarExportResponseDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.ProfileDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.UpdateAuthProfileDto
import com.crm.enhance_wellness.feature.notifications.data.remote.dto.PortalNotificationsResponseDto
import com.crm.enhance_wellness.feature.finance.data.remote.dto.PaymentConfigDto
import com.crm.enhance_wellness.feature.finance.data.remote.dto.PaymentDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardConfirmDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardConfirmResponseDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardOrderDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardOrderResponseDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.GiftCardStorefrontResponseDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.MyTransactionsResponseDto
import com.crm.enhance_wellness.feature.wallet.data.remote.dto.PatientWalletResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WellnessApiService {

    // ── Tenant Branding ──────────────────────────────────────────────────────
    @GET("public/tenant/{slug}")
    suspend fun getTenantBranding(
        @Path("slug") slug: String,
    ): Response<TenantBrandingResponseDto>

    // ── Auth — absolute paths (/api/auth/* not under /api/wellness/) ──────────
    @POST("/api/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): Response<LoginResponseDto>

    @POST("/api/auth/customer/register")
    suspend fun registerCustomer(
        @Body body: RegisterRequestDto,
    ): Response<RegisterResponseDto>

    @POST("/api/auth/check-email")
    suspend fun checkEmail(@Body body: CheckEmailRequestDto): Response<CheckEmailResponseDto>

    @POST("/api/auth/email-otp/request")
    suspend fun requestEmailOtp(@Body body: EmailOtpRequestDto): Response<Unit>

    @POST("/api/auth/email-otp/verify")
    suspend fun verifyEmailOtp(@Body body: EmailOtpVerifyRequestDto): Response<EmailOtpVerifyResponseDto>

    // ── Portal Health (SMS availability check) ───────────────────────────────
    @GET("portal/health")
    suspend fun getPortalHealth(): Response<PortalHealthDto>

    // ── Patient Permissions ───────────────────────────────────────────────────
    @GET("portal/me/permissions")
    suspend fun getPatientPermissions(): Response<PatientPermissionsDto>

    // ── Profile ───────────────────────────────────────────────────────────────
    // GET /portal/me — patient-layer: name, phone, email, dob, gender, id (patientId)
    @GET("portal/me")
    suspend fun getProfile(): Response<ProfileDto>

    // GET /api/auth/me — user-layer: name, email, role, profilePicture
    @GET("/api/auth/me")
    suspend fun getAuthProfile(): Response<AuthProfileResponseDto>

    // PUT /api/auth/me — update name, email, or password (dob/gender/phone not supported)
    @PUT("/api/auth/me")
    suspend fun updateAuthProfile(
        @Body body: UpdateAuthProfileDto,
    ): Response<AuthProfileResponseDto>

    @Multipart
    @POST("/api/auth/me/profile-picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part,
    ): Response<AuthProfileResponseDto>

    @DELETE("/api/auth/me/profile-picture")
    suspend fun deleteProfilePicture(): Response<AuthProfileResponseDto>

    @POST("portal/export")
    suspend fun requestDsarExport(): Response<DsarExportResponseDto>

    @DELETE("/api/auth/me/account")
    suspend fun deleteAccount(
        @Body body: DeleteAccountRequestDto,
    ): Response<DeleteAccountResponseDto>

    // ── Portal Notifications ──────────────────────────────────────────────────
    @GET("portal/me/notifications")
    suspend fun getPortalNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): Response<PortalNotificationsResponseDto>

    @PUT("portal/me/notifications/{id}/read")
    suspend fun markPortalNotificationRead(@Path("id") id: String): Response<Unit>

    @POST("portal/me/notifications/read-all")
    suspend fun markAllPortalNotificationsRead(): Response<Unit>

    // ── Visits ────────────────────────────────────────────────────────────────
    @GET("portal/visits")
    suspend fun getVisits(
        @Query("upcoming") upcoming: Boolean? = null,
    ): Response<List<VisitDto>>

    // ── Appointments (portal-scoped, verifyPatientToken) ──────────────────────
    // bucket = "upcoming" | "past" | "all" (default: "upcoming")
    @GET("portal/appointments")
    suspend fun getMyAppointments(
        @Query("bucket") bucket: String? = null,
    ): Response<AppointmentListResponseDto>

    @POST("portal/appointments/book")
    suspend fun bookAppointment(
        @Body body: BookAppointmentDto,
    ): Response<BookAppointmentResponseDto>

    @POST("portal/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") appointmentId: Int,
    ): Response<CancelAppointmentResponseDto>

    @PATCH("portal/appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("id") appointmentId: Int,
        @Body body: RescheduleAppointmentDto,
    ): Response<RescheduleAppointmentResponseDto>

    // ── Waitlist ──────────────────────────────────────────────────────────────
    @GET("waitlist")
    suspend fun getWaitlist(): Response<List<WaitlistEntryDto>>

    @POST("waitlist")
    suspend fun addToWaitlist(@Body body: AddWaitlistDto): Response<WaitlistEntryDto>

    // ── Products / Services (patient-facing catalogue) ────────────────────────
    // portal/products and portal/product-categories both require products.read, which is
    // not granted to the CUSTOMER role — they returned 403 for every patient. The public
    // services endpoints below carry the same catalogue and are used instead.
    @GET("services")
    suspend fun getPortalProducts(@Query("public") public: Boolean = true): Response<List<ProductDto>>

    // ── Catalog (catalog feature — own DTOs, no cross-feature import from booking) ──
    @GET("services")
    suspend fun getCatalogServices(@Query("public") public: Boolean = true): Response<List<CatalogServiceDto>>

    @GET("service-categories")
    suspend fun getCatalogServiceCategories(@Query("public") public: Boolean = true): Response<List<CatalogServiceCategoryDto>>

    // GET /api/wellness/packages — CUSTOMER JWT.
    // Reverse-engineered from the patient web portal's Catalog > Packages tab (2026-08-31);
    // only the read path is confirmed — purchase flow not yet implemented.
    @GET("packages")
    suspend fun getServicePackages(): Response<ServicePackagesResponseDto>

    // ── Prescriptions ─────────────────────────────────────────────────────────
    @GET("portal/prescriptions")
    suspend fun getPrescriptions(): Response<List<PrescriptionDto>>

    @GET("portal/prescriptions/{id}/pdf")
    suspend fun getPrescriptionPdf(
        @Path("id") prescriptionId: Int,
    ): Response<ResponseBody>

    // Used by the "Request Meds" action to pull the backend's own computed validUntil per
    // prescription for the depletion reminder — see MyPrescriptionsResponseDto.
    @GET("my-prescriptions")
    suspend fun getMyPrescriptions(): Response<MyPrescriptionsResponseDto>

    // Read-only refill/prescription request status list — see PrescriptionRequestDto.
    // NOT live on this app's configured BuildConfig.BASE_URL host (globuscrm.globussoft.com) —
    // confirmed there via a valid device token: clean 404 API_ROUTE_NOT_FOUND, route unregistered.
    // Confirmed present on https://crm.globusdemos.com/api/wellness/ instead (CLAUDE.md's
    // documented "Production" host, which this app is NOT actually pointed at — see
    // build.gradle.kts): same literal path returns 401 "Invalid or expired portal token", a
    // token-specific rejection rather than a route-not-found, i.e. the route exists there.
    // Path relative to the base is correct either way; only the host differs.
    // Returns the raw body (not List<PrescriptionRequestDto> directly): the backend sends an
    // EMPTY body rather than `[]` when a patient has no requests, which crashes Moshi's
    // strict parser at the root; PrescriptionRequestRepositoryImpl parses defensively.
    @GET("portal/prescription-requests")
    suspend fun getPrescriptionRequests(): Response<ResponseBody>

    // Same path, POST — submits a renewal/refill request. See SubmitPrescriptionRequestDto.
    @POST("portal/prescription-requests")
    suspend fun submitPrescriptionRequest(
        @Body body: SubmitPrescriptionRequestDto,
    ): Response<ResponseBody>

    @Multipart
    @POST("visits/{visitId}/photos")
    suspend fun uploadVisitTreatmentPhoto(
        @Path("visitId") visitId: Int,
        @Part photos: MultipartBody.Part,
        @Part("kind") kind: RequestBody,
    ): Response<ResponseBody>

    // ── Treatment Plans (Phase 2 UI, data layer ready) ───────────────────────
    // Uses patient-row ID from EncryptedPrefsManager.getPatientId().
    @GET("patients/{patientId}/treatment-plans")
    suspend fun getTreatmentPlans(
        @Path("patientId") patientId: Int,
    ): Response<List<TreatmentPlanDto>>

    // ── Consent Forms (Phase 2 UI, data layer ready) ─────────────────────────
    // JWT-scoped: returns both legacy consents and e-signature records for the caller.
    @GET("portal/consents")
    suspend fun getConsents(): Response<List<ConsentFormDto>>

    @GET("portal/consents/{id}/pdf")
    suspend fun getConsentPdf(
        @Path("id") consentId: Int,
    ): Response<ResponseBody>

    // E-signature consents: ids live in a different table than legacy consents.
    @GET("portal/signatures/{requestId}/pdf")
    suspend fun getSignaturePdf(
        @Path("requestId") requestId: Int,
    ): Response<ResponseBody>

    // ── Memberships ───────────────────────────────────────────────────────────
    // GET /appointments/my-memberships — returns patient's active/past memberships.
    @GET("appointments/my-memberships")
    suspend fun getMyMemberships(): Response<List<MembershipDto>>

    // GET /membership-plans — full catalog for plan-browse screen.
    @GET("membership-plans")
    suspend fun getMembershipPlans(): Response<List<MembershipPlanDto>>

    // ── Wallet ────────────────────────────────────────────────────────────────
    // GET /patients/{patientId}/wallet — dedicated wallet view with balance + wallet-only txns.
    @GET("patients/{patientId}/wallet")
    suspend fun getPatientWallet(
        @Path("patientId") patientId: Int,
    ): Response<PatientWalletResponseDto>

    // GET /my-transactions — unified timeline across all transaction types.
    // Also used by DashboardRepositoryImpl for summary.walletBalance.
    @GET("my-transactions")
    suspend fun getMyTransactions(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): Response<MyTransactionsResponseDto>

    // ── Loyalty (Phase 2 UI, data layer ready) ────────────────────────────────
    // ⚠️ SECURITY: backend does not verify caller owns this patientId. Use only
    // with EncryptedPrefsManager.getPatientId() — never accept patientId from user input.
    @GET("loyalty/{patientId}")
    suspend fun getLoyalty(
        @Path("patientId") patientId: Int,
    ): Response<LoyaltyResponseDto>

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

    // ── Doctors ───────────────────────────────────────────────────────────────
    @GET("doctors/availability")
    suspend fun getDoctorAvailability(
        @Query("date") date: String,
    ): Response<List<com.crm.enhance_wellness.feature.booking.data.remote.dto.DoctorAvailabilityDto>>

    // ── Payments ─────────────────────────────────────────────────────────────
    @GET("/api/payments")
    suspend fun getPayments(): Response<List<PaymentDto>>

    @GET("/api/payments/config")
    suspend fun getPaymentConfig(): Response<PaymentConfigDto>

}
