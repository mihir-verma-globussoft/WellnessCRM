package com.crm.enhance_wellness.feature.health.reminder

import com.crm.enhance_wellness.feature.health.domain.model.Drug
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MedicationReminderModelsTest {

    @Test
    fun `toScheduledMedication accepts numeric API values`() {
        val medication = Drug(
            name = "Amoxicillin",
            dosage = "1",
            frequency = "3",
            duration = "5",
            instructions = null,
        ).toScheduledMedication()

        assertNotNull(medication)
        assertEquals(3, medication?.frequencyPerDay)
        assertEquals(5, medication?.durationDays)
    }

    @Test
    fun `toScheduledMedication accepts human-readable API values`() {
        val medication = Drug(
            name = "Vitamin C",
            dosage = "500mg",
            frequency = "Every 8 hours",
            duration = "1 week",
            instructions = null,
        ).toScheduledMedication()

        assertNotNull(medication)
        assertEquals(3, medication?.frequencyPerDay)
        assertEquals(7, medication?.durationDays)
    }

    @Test
    fun `toScheduledMedication accepts dose timing shorthand`() {
        val medication = Drug(
            name = "Painkiller",
            dosage = "1",
            frequency = "1-0-1",
            duration = "10 days",
            instructions = null,
        ).toScheduledMedication()

        assertNotNull(medication)
        assertEquals(2, medication?.frequencyPerDay)
        assertEquals(10, medication?.durationDays)
    }

    @Test
    fun `toScheduledMedication defaults missing frequency and duration to schedule reminders`() {
        val medication = Drug(
            name = "Levocetirizine 5mg",
            dosage = "1",
            frequency = null,
            duration = "7",
            instructions = null,
        ).toScheduledMedication()

        assertNotNull(medication)
        assertEquals(1, medication?.frequencyPerDay)
        assertEquals(7, medication?.durationDays)
    }
}
