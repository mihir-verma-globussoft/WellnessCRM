package com.crm.enhance_wellness.feature.health.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crm.enhance_wellness.feature.health.data.local.entity.PrescriptionReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionReminderDao {

    @Query("SELECT * FROM prescription_reminders")
    fun getAllAsFlow(): Flow<List<PrescriptionReminderEntity>>

    @Query("SELECT * FROM prescription_reminders")
    suspend fun getAll(): List<PrescriptionReminderEntity>

    @Query("SELECT * FROM prescription_reminders WHERE prescriptionId = :prescriptionId")
    suspend fun getByPrescriptionId(prescriptionId: Int): PrescriptionReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: PrescriptionReminderEntity)

    @Query("DELETE FROM prescription_reminders WHERE prescriptionId = :prescriptionId")
    suspend fun deleteByPrescriptionId(prescriptionId: Int)

    @Delete
    suspend fun delete(reminder: PrescriptionReminderEntity)

    @Query("DELETE FROM prescription_reminders WHERE endAt <= :now")
    suspend fun deleteExpired(now: Long)
}
