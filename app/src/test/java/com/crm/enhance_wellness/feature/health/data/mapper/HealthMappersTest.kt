package com.crm.enhance_wellness.feature.health.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
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
}
