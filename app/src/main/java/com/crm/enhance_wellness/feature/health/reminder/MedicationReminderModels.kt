package com.crm.enhance_wellness.feature.health.reminder

import com.crm.enhance_wellness.feature.health.domain.model.Drug
import org.json.JSONArray
import org.json.JSONObject

data class ScheduledMedication(
    val name: String,
    val dosage: String?,
    val frequencyPerDay: Int,
    val durationDays: Int,
)

fun Drug.toScheduledMedication(): ScheduledMedication? {
    val frequency = frequency?.toIntFromApiValue() ?: return null
    val duration = duration?.toIntFromApiValue() ?: return null
    if (name.isBlank() || frequency <= 0 || duration <= 0) return null
    return ScheduledMedication(
        name = name,
        dosage = dosage,
        frequencyPerDay = frequency,
        durationDays = duration,
    )
}

fun List<ScheduledMedication>.toReminderJson(): String {
    val array = JSONArray()
    forEach { drug ->
        array.put(
            JSONObject()
                .put("name", drug.name)
                .put("dosage", drug.dosage)
                .put("frequencyPerDay", drug.frequencyPerDay)
                .put("durationDays", drug.durationDays)
        )
    }
    return array.toString()
}

fun parseReminderDrugs(json: String): List<ScheduledMedication> = runCatching {
    val array = JSONArray(json)
    (0 until array.length()).mapNotNull { index ->
        val obj = array.optJSONObject(index) ?: return@mapNotNull null
        val name = obj.optString("name")
        val frequency = obj.optInt("frequencyPerDay", 0)
        val duration = obj.optInt("durationDays", 0)
        if (name.isBlank() || frequency <= 0 || duration <= 0) return@mapNotNull null
        ScheduledMedication(
            name = name,
            dosage = obj.optString("dosage").ifBlank { null },
            frequencyPerDay = frequency,
            durationDays = duration,
        )
    }
}.getOrDefault(emptyList())

private fun String.toIntFromApiValue(): Int? {
    val numeric = trim().takeWhile { it.isDigit() || it == '.' }
    return numeric.toDoubleOrNull()?.toInt()
}
