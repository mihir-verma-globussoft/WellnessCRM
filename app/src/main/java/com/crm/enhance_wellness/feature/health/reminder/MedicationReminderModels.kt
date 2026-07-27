package com.crm.enhance_wellness.feature.health.reminder

import com.crm.enhance_wellness.feature.health.domain.model.Drug
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class ScheduledMedication(
    val name: String,
    val dosage: String?,
    val frequencyPerDay: Int,
    val durationDays: Int,
)

fun Drug.toScheduledMedication(): ScheduledMedication? {
    val frequency = frequency?.toFrequencyPerDay() ?: 1
    val duration = duration?.toDurationDays() ?: 7
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

internal fun String.toFrequencyPerDay(): Int? {
    val value = normalizedApiValue()
    if (value.isBlank()) return null

    parseDosePattern(value)?.let { return it }
    parseFrequencyWord(value)?.let { return it }

    val number = value.firstNumber() ?: return null
    if ("hour" in value || "hr" in value) {
        if (number <= 0.0) return null
        return (24.0 / number).roundToInt().coerceAtLeast(1)
    }
    return number.toInt().takeIf { it > 0 }
}

internal fun String.toDurationDays(): Int? {
    val value = normalizedApiValue()
    if (value.isBlank()) return null
    val number = value.firstNumber() ?: parseNumberWord(value)?.toDouble() ?: return null
    val days = when {
        "week" in value -> number * 7
        "month" in value -> number * 30
        else -> number
    }
    return days.toInt().takeIf { it > 0 }
}

private fun String.normalizedApiValue(): String =
    trim().lowercase()

private fun String.firstNumber(): Double? =
    Regex("""\d+(?:\.\d+)?""").find(this)?.value?.toDoubleOrNull()

private fun parseDosePattern(value: String): Int? {
    if (!Regex("""^\d+(?:\s*[-+/]\s*\d+)+$""").matches(value)) return null
    val total = Regex("""\d+""")
        .findAll(value)
        .sumOf { it.value.toIntOrNull() ?: 0 }
    return total.takeIf { it > 0 }
}

private fun parseFrequencyWord(value: String): Int? = when {
    "qid" in value || "qds" in value || "four times" in value -> 4
    "tds" in value || "tid" in value || "thrice" in value || "three times" in value -> 3
    "bd" in value || "bid" in value || "twice" in value || "two times" in value -> 2
    "od" in value || "once" in value || "daily" in value -> 1
    else -> parseNumberWord(value)
}

private fun parseNumberWord(value: String): Int? = when {
    "one" in value -> 1
    "two" in value -> 2
    "three" in value -> 3
    "four" in value -> 4
    "five" in value -> 5
    "six" in value -> 6
    "seven" in value -> 7
    "eight" in value -> 8
    "nine" in value -> 9
    "ten" in value -> 10
    else -> null
}
