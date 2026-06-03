package com.globussoft.wellness.patient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.globussoft.wellness.patient.feature.booking.data.local.dao.VisitDao
import com.globussoft.wellness.patient.feature.booking.data.local.entity.CachedVisit
import com.globussoft.wellness.patient.feature.health.data.local.dao.PrescriptionDao
import com.globussoft.wellness.patient.feature.health.data.local.entity.CachedPrescription
import com.globussoft.wellness.patient.feature.membership.data.local.dao.MembershipDao
import com.globussoft.wellness.patient.feature.membership.data.local.entity.CachedMembership
import com.globussoft.wellness.patient.feature.notifications.data.local.dao.NotificationDao
import com.globussoft.wellness.patient.feature.notifications.data.local.entity.CachedNotification

@Database(
    entities = [
        CachedVisit::class,
        CachedPrescription::class,
        CachedMembership::class,
        CachedNotification::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitDao(): VisitDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun membershipDao(): MembershipDao
    abstract fun notificationDao(): NotificationDao
}
