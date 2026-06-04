package com.globussoft.wellness.patient.core.di

import android.content.Context
import androidx.room.Room
import com.globussoft.wellness.patient.core.database.AppDatabase
import com.globussoft.wellness.patient.feature.booking.data.local.dao.VisitDao
import com.globussoft.wellness.patient.feature.health.data.local.dao.PrescriptionDao
import com.globussoft.wellness.patient.feature.membership.data.local.dao.MembershipDao
import com.globussoft.wellness.patient.feature.notifications.data.local.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wellness_db")
            .build()

    @Provides
    fun provideVisitDao(db: AppDatabase): VisitDao = db.visitDao()

    @Provides
    fun providePrescriptionDao(db: AppDatabase): PrescriptionDao = db.prescriptionDao()

    @Provides
    fun provideMembershipDao(db: AppDatabase): MembershipDao = db.membershipDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
