package com.crm.enhance_wellness.feature.health.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrescriptionRequestTest {

    @Test
    fun `toRefillStatusLabel maps backend statuses to display labels`() {
        assertEquals("Pending", "PENDING".toRefillStatusLabel())
        assertEquals("Approved", "ACCEPTED".toRefillStatusLabel())
        assertEquals("Rejected", "REJECTED".toRefillStatusLabel())
        assertEquals("Completed", "COMPLETED".toRefillStatusLabel())
    }

    @Test
    fun `toRefillStatusLabel is case-insensitive`() {
        assertEquals("Pending", "pending".toRefillStatusLabel())
        assertEquals("Approved", "Accepted".toRefillStatusLabel())
    }

    @Test
    fun `toRefillStatusLabel falls back to capitalizing an unknown status`() {
        assertEquals("Draft", "draft".toRefillStatusLabel())
    }

    @Test
    fun `drugNames prefers requestedDrugNames over prescriptionDrugNames`() {
        val request = samplePrescriptionRequest(
            requestedDrugNames = listOf("Azithromycin 500mg"),
            prescriptionDrugNames = listOf("Azithromycin 500mg", "Biotin 10000mcg"),
        )

        assertEquals(listOf("Azithromycin 500mg"), request.drugNames)
    }

    @Test
    fun `drugNames falls back to prescriptionDrugNames when nothing was specifically requested`() {
        val request = samplePrescriptionRequest(
            requestedDrugNames = emptyList(),
            prescriptionDrugNames = listOf("Minoxidil .5mg"),
        )

        assertEquals(listOf("Minoxidil .5mg"), request.drugNames)
    }

    private fun samplePrescriptionRequest(
        requestedDrugNames: List<String>,
        prescriptionDrugNames: List<String>,
    ) = PrescriptionRequest(
        id = 1,
        status = "PENDING",
        prescriptionId = 1,
        doctorName = null,
        isFullPrescription = requestedDrugNames.isEmpty(),
        requestedDrugNames = requestedDrugNames,
        prescriptionDrugNames = prescriptionDrugNames,
        requestedDurationDays = null,
        notes = null,
        reviewedByName = null,
        reviewedAt = null,
        reviewNote = null,
        createdAt = null,
    )
}
