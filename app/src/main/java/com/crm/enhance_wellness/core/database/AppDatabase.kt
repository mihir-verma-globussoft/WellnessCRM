package com.crm.enhance_wellness.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crm.enhance_wellness.feature.booking.data.local.dao.VisitDao
import com.crm.enhance_wellness.feature.booking.data.local.entity.CachedVisit
import com.crm.enhance_wellness.feature.health.data.local.dao.PrescriptionDao
import com.crm.enhance_wellness.feature.health.data.local.entity.CachedPrescription
import com.crm.enhance_wellness.feature.membership.data.local.dao.MembershipDao
import com.crm.enhance_wellness.feature.membership.data.local.entity.CachedMembership
import com.crm.enhance_wellness.feature.notifications.data.local.dao.NotificationDao
import com.crm.enhance_wellness.feature.notifications.data.local.entity.CachedNotification

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
