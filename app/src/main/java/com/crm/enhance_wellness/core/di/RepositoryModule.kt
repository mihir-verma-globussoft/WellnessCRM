package com.crm.enhance_wellness.core.di

import com.crm.enhance_wellness.feature.catalog.data.repository.CatalogRepositoryImpl
import com.crm.enhance_wellness.feature.catalog.domain.repository.CatalogRepository
import com.crm.enhance_wellness.feature.finance.data.repository.FinanceRepositoryImpl
import com.crm.enhance_wellness.feature.finance.domain.repository.FinanceRepository
import com.crm.enhance_wellness.feature.auth.data.repository.AuthRepositoryImpl
import com.crm.enhance_wellness.feature.auth.domain.repository.AuthRepository
import com.crm.enhance_wellness.feature.health.data.repository.ConsentFormRepositoryImpl
import com.crm.enhance_wellness.feature.health.data.repository.TreatmentPlanRepositoryImpl
import com.crm.enhance_wellness.feature.health.domain.repository.ConsentFormRepository
import com.crm.enhance_wellness.feature.health.domain.repository.TreatmentPlanRepository
import com.crm.enhance_wellness.feature.loyalty.data.repository.LoyaltyRepositoryImpl
import com.crm.enhance_wellness.feature.loyalty.domain.repository.LoyaltyRepository
import com.crm.enhance_wellness.feature.booking.data.repository.AppointmentRepositoryImpl
import com.crm.enhance_wellness.feature.booking.domain.repository.AppointmentRepository
import com.crm.enhance_wellness.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.crm.enhance_wellness.feature.dashboard.domain.repository.DashboardRepository
import com.crm.enhance_wellness.feature.health.data.repository.PrescriptionRepositoryImpl
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRepository
import com.crm.enhance_wellness.feature.membership.data.repository.MembershipRepositoryImpl
import com.crm.enhance_wellness.feature.membership.domain.repository.MembershipRepository
import com.crm.enhance_wellness.feature.notifications.data.repository.NotificationRepositoryImpl
import com.crm.enhance_wellness.feature.notifications.domain.repository.NotificationRepository
import com.crm.enhance_wellness.feature.profile.data.repository.ProfileRepositoryImpl
import com.crm.enhance_wellness.feature.profile.domain.repository.ProfileRepository
import com.crm.enhance_wellness.feature.wallet.data.repository.GiftCardRepositoryImpl
import com.crm.enhance_wellness.feature.wallet.data.repository.WalletRepositoryImpl
import com.crm.enhance_wellness.feature.wallet.domain.repository.GiftCardRepository
import com.crm.enhance_wellness.feature.wallet.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository

    @Binds @Singleton
    abstract fun bindPrescriptionRepository(impl: PrescriptionRepositoryImpl): PrescriptionRepository

    @Binds @Singleton
    abstract fun bindMembershipRepository(impl: MembershipRepositoryImpl): MembershipRepository

    @Binds @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds @Singleton
    abstract fun bindGiftCardRepository(impl: GiftCardRepositoryImpl): GiftCardRepository

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindTreatmentPlanRepository(impl: TreatmentPlanRepositoryImpl): TreatmentPlanRepository

    @Binds @Singleton
    abstract fun bindConsentFormRepository(impl: ConsentFormRepositoryImpl): ConsentFormRepository

    @Binds @Singleton
    abstract fun bindLoyaltyRepository(impl: LoyaltyRepositoryImpl): LoyaltyRepository

    @Binds @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds @Singleton
    abstract fun bindFinanceRepository(impl: FinanceRepositoryImpl): FinanceRepository
}
