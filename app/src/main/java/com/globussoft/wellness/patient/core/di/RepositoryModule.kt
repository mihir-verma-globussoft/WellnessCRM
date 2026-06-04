package com.globussoft.wellness.patient.core.di

import com.globussoft.wellness.patient.feature.auth.data.repository.AuthRepositoryImpl
import com.globussoft.wellness.patient.feature.auth.domain.repository.AuthRepository
import com.globussoft.wellness.patient.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.globussoft.wellness.patient.feature.dashboard.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
