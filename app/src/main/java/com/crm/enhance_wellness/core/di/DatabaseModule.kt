package com.crm.enhance_wellness.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crm.enhance_wellness.core.database.AppDatabase
import com.crm.enhance_wellness.feature.booking.data.local.dao.VisitDao
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionDao
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionReminderDao
import com.crm.enhance_wellness.feature.membership.data.local.dao.MembershipDao
import com.crm.enhance_wellness.feature.notifications.data.local.dao.NotificationDao
import com.crm.enhance_wellness.feature.treatmentanalysis.data.local.dao.TreatmentAnalysisDraftDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideVisitDao(db: AppDatabase): VisitDao = db.visitDao()

    @Provides
    fun providePrescriptionDao(db: AppDatabase): PrescriptionDao = db.prescriptionDao()

    @Provides
    fun providePrescriptionReminderDao(db: AppDatabase): PrescriptionReminderDao =
        db.prescriptionReminderDao()

    @Provides
    fun provideMembershipDao(db: AppDatabase): MembershipDao = db.membershipDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideTreatmentAnalysisDraftDao(db: AppDatabase): TreatmentAnalysisDraftDao =
        db.treatmentAnalysisDraftDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS prescription_reminders (
                    prescriptionId INTEGER NOT NULL,
                    enabledAt INTEGER NOT NULL,
                    startAt INTEGER NOT NULL,
                    endAt INTEGER NOT NULL,
                    prescriptionLabel TEXT,
                    drugsJson TEXT NOT NULL,
                    PRIMARY KEY(prescriptionId)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS treatment_analysis_drafts (
                    prescriptionId INTEGER NOT NULL,
                    analysisId INTEGER,
                    beforeLocalPath TEXT,
                    beforeRemoteUrl TEXT,
                    beforeCapturedAt INTEGER,
                    afterLocalPath TEXT,
                    afterRemoteUrl TEXT,
                    afterCapturedAt INTEGER,
                    status TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(prescriptionId)
                )
                """.trimIndent()
            )
        }
    }
}
