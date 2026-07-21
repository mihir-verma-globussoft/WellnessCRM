package com.crm.enhance_wellness.feature.health.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescription_reminders")
data class PrescriptionReminderEntity(
    @PrimaryKey val prescriptionId: Int,
    val enabledAt: Long,
    val startAt: Long,
    val endAt: Long,
    val prescriptionLabel: String?,
    val drugsJson: String,
)
