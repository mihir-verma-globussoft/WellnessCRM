package com.crm.enhance_wellness.core.di

import android.content.Context
import androidx.room.Room
import com.crm.enhance_wellness.core.database.AppDatabase
import com.crm.enhance_wellness.feature.booking.data.local.dao.VisitDao
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionDao
import com.crm.enhance_wellness.feature.membership.data.local.dao.MembershipDao
import com.crm.enhance_wellness.feature.notifications.data.local.dao.NotificationDao
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
