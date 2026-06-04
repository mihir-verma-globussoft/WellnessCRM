package com.globussoft.wellness.patient.core.di

import com.globussoft.wellness.patient.feature.auth.data.repository.AuthRepositoryImpl
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import com.globussoft.wellness.patient.feature.booking.data.repository.AppointmentRepositoryImpl
import com.globussoft.wellness.patient.feature.booking.domain.repository.AppointmentRepository
import com.globussoft.wellness.patient.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.globussoft.wellness.patient.feature.dashboard.domain.repository.DashboardRepository
import com.globussoft.wellness.patient.feature.health.data.repository.PrescriptionRepositoryImpl
import com.globussoft.wellness.patient.feature.health.domain.repository.PrescriptionRepository
import com.globussoft.wellness.patient.feature.membership.data.repository.MembershipRepositoryImpl
import com.globussoft.wellness.patient.feature.membership.domain.repository.MembershipRepository
import com.globussoft.wellness.patient.feature.notifications.data.repository.NotificationRepositoryImpl
import com.globussoft.wellness.patient.feature.notifications.domain.repository.NotificationRepository
import com.globussoft.wellness.patient.feature.profile.data.repository.ProfileRepositoryImpl
import com.globussoft.wellness.patient.feature.profile.domain.repository.ProfileRepository
import com.globussoft.wellness.patient.feature.wallet.data.repository.GiftCardRepositoryImpl
import com.globussoft.wellness.patient.feature.wallet.data.repository.WalletRepositoryImpl
import com.globussoft.wellness.patient.feature.wallet.domain.repository.GiftCardRepository
import com.globussoft.wellness.patient.feature.wallet.domain.repository.WalletRepository
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
}
