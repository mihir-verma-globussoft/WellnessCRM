package com.globussoft.wellness.patient.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Repository @Binds declarations are added here as each feature is implemented (Phase 2 onwards).
// Each feature adds its own @Binds entry:
//   @Binds abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule
