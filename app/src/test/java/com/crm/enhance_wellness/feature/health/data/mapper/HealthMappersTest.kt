package com.crm.enhance_wellness.feature.health.data.mapper

import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionRequestDrugDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionRequestDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.PrescriptionRequestPrescriptionDto
import com.crm.enhance_wellness.feature.health.data.remote.dto.RequestedDrugDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HealthMappersTest {

    @Test
    fun `parseDrugsJson supports wrapped backend drug payload`() {
        val json = """
            {
              "drugs": [
                {
                  "drugName": "Amoxicillin",
                  "dose": 1,
                  "frequencyPerDay": 3,
                  "durationDays": 5,
                  "notes": "After food"
                }
              ]
            }
        """.trimIndent()

        val drugs = parseDrugsJson(json)

        assertEquals(1, drugs.size)
        assertEquals("Amoxicillin", drugs.first().name)
        assertEquals("1", drugs.first().dosage)
        assertEquals("3", drugs.first().frequency)
        assertEquals("5", drugs.first().duration)
        assertEquals("After food", drugs.first().instructions)
    }

    @Test
    fun `parseDrugsJson supports nested medicine name variants`() {
        val json = """
            [
              {
                "medicine": { "name": "Vitamin C" },
                "dosage": "500mg",
                "frequency": "1-0-1",
                "duration": "1 week"
              }
            ]
        """.trimIndent()

        val drug = parseDrugsJson(json).first()

        assertEquals("Vitamin C", drug.name)
        assertEquals("500mg", drug.dosage)
        assertEquals("1-0-1", drug.frequency)
        assertEquals("1 week", drug.duration)
    }

    @Test
    fun `parseDrugsJson returns empty list for null or blank input`() {
        assertEquals(emptyList<Any>(), parseDrugsJson(null))
        assertEquals(emptyList<Any>(), parseDrugsJson(""))
        assertEquals(emptyList<Any>(), parseDrugsJson("null"))
    }

    @Test
    fun `PrescriptionRequestDto toDomain uses requestedDrugs for a partial refill`() {
        val dto = PrescriptionRequestDto(
            id = 5,
            status = "PENDING",
            prescriptionId = 367,
            doctorName = "Pratibha Laxmi Singh",
            isFullPrescription = false,
            requestedDrugs = listOf(RequestedDrugDto(name = "Azithromycin 500mg")),
            requestedDurationDays = 60,
            notes = "Running low, please repeat just the tablets.",
            reviewedByName = null,
            reviewedAt = null,
            reviewNote = null,
            createdAt = "2026-08-24T10:23:05.744Z",
            prescription = PrescriptionRequestPrescriptionDto(
                id = 367,
                drugs = listOf(
                    PrescriptionRequestDrugDto(name = "Azithromycin 500mg"),
                    PrescriptionRequestDrugDto(name = "Biotin 10000mcg"),
                ),
            ),
        )

        val domain = dto.toDomain()

        assertEquals(5, domain.id)
        assertEquals("PENDING", domain.status)
        assertEquals(listOf("Azithromycin 500mg"), domain.requestedDrugNames)
        assertEquals(listOf("Azithromycin 500mg", "Biotin 10000mcg"), domain.prescriptionDrugNames)
        assertEquals(listOf("Azithromycin 500mg"), domain.drugNames)
    }

    @Test
    fun `PrescriptionRequestDto toDomain falls back to prescription drugs for a full refill`() {
        val dto = PrescriptionRequestDto(
            id = 12,
            status = "PENDING",
            prescriptionId = 427,
            doctorName = "Ganesh Sharma",
            isFullPrescription = true,
            requestedDrugs = null,
            requestedDurationDays = null,
            notes = null,
            reviewedByName = null,
            reviewedAt = null,
            reviewNote = null,
            createdAt = "2026-08-27T11:28:40.804Z",
            prescription = PrescriptionRequestPrescriptionDto(
                id = 427,
                drugs = listOf(PrescriptionRequestDrugDto(name = "Minoxidil .5mg")),
            ),
        )

        val domain = dto.toDomain()

        assertTrue(domain.requestedDrugNames.isEmpty())
        assertEquals(listOf("Minoxidil .5mg"), domain.drugNames)
    }

    @Test
    fun `PrescriptionRequestDto toDomain handles a null prescription and requestedDrugs gracefully`() {
        val dto = PrescriptionRequestDto(
            id = 1,
            status = "PENDING",
            prescriptionId = 1,
            doctorName = null,
            isFullPrescription = true,
            requestedDrugs = null,
            requestedDurationDays = null,
            notes = null,
            reviewedByName = null,
            reviewedAt = null,
            reviewNote = null,
            createdAt = null,
            prescription = null,
        )

        val domain = dto.toDomain()

        assertTrue(domain.drugNames.isEmpty())
    }
}
