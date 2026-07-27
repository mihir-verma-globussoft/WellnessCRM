package com.crm.enhance_wellness.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtil {

    private val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val iso8601Short = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val displayDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    private val displayDateTime = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
    private val displayMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val apiDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val apiTime = SimpleDateFormat("HH:mm", Locale.US)

    fun parseIso8601(iso: String?): Date? = iso?.let { isoStr ->
        runCatching { iso8601.parse(isoStr) }.getOrElse { runCatching { iso8601Short.parse(isoStr) }.getOrNull() }
    }

    /** Parses ISO8601 datetimes and plain "yyyy-MM-dd" dates. */
    fun parseFlexibleDate(value: String?): Date? = value?.let { v ->
        parseIso8601(v) ?: runCatching { apiDate.parse(v) }.getOrNull()
    }

    fun toDisplayDate(iso: String?): String = parseFlexibleDate(iso)?.let { displayDate.format(it) } ?: "—"

    fun toDisplayDateTime(iso: String?): String = parseFlexibleDate(iso)?.let { displayDateTime.format(it) } ?: "—"

    fun toDisplayMonthYear(iso: String?): String = parseFlexibleDate(iso)?.let { displayMonthYear.format(it) } ?: "—"

    fun toDisplayDate(epochMs: Long): String = displayDate.format(Date(epochMs))

    fun toDisplayDateTime(epochMs: Long): String = displayDateTime.format(Date(epochMs))

    fun toDisplayMonthYear(epochMs: Long): String = displayMonthYear.format(Date(epochMs))

    fun isoToEpochMs(iso: String?): Long = parseFlexibleDate(iso)?.time ?: 0L

    fun epochMsToIso(epochMs: Long): String = iso8601.format(Date(epochMs))

    fun toApiDate(epochMs: Long): String = apiDate.format(Date(epochMs))

    fun apiDateToEpochMs(apiDateStr: String?): Long = apiDateStr?.let {
        runCatching { apiDate.parse(it)?.time }.getOrNull()
    } ?: 0L

    fun todayApiDate(): String = apiDate.format(Date())

    fun toApiTime(epochMs: Long): String = apiTime.format(Date(epochMs))

    /** UTC millis for the start of the current local day. */
    fun startOfTodayUtcMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * The Compose DatePicker returns UTC-midnight millis for the *displayed* local date.
     * Adding the local timezone offset converts it back to the corresponding local-midnight
     * UTC millis, so all subsequent formatting/comparisons match what the user picked.
     */
    fun datePickerMillisToLocalMidnight(utcMillis: Long): Long {
        return utcMillis + TimeZone.getDefault().getOffset(utcMillis)
    }

    /** True when [dateEpochMs] (already normalized or from the date picker) falls on the current local day. */
    fun isToday(dateEpochMs: Long): Boolean {
        val normalized = datePickerMillisToLocalMidnight(dateEpochMs)
        return toApiDate(normalized) == todayApiDate()
    }

    /** True when the UTC-midnight millis from the date picker represents today or a future local day. */
    fun isTodayOrFutureDate(utcMillis: Long): Boolean {
        val localMidnight = datePickerMillisToLocalMidnight(utcMillis)
        return localMidnight >= startOfTodayUtcMillis()
    }

    /**
     * Generate bookable time slots for a date in HH:mm format.
     * For today, slots in the past or within [bufferMinutes] are omitted.
     */
    fun generateTimeSlots(
        dateEpochMs: Long,
        startHour: Int = 9,
        endHour: Int = 18,
        intervalMinutes: Int = 30,
        bufferMinutes: Int = 30,
    ): List<String> {
        val slots = mutableListOf<String>()
        val localMidnight = datePickerMillisToLocalMidnight(dateEpochMs)
        val isToday = isToday(dateEpochMs)
        val bufferCal = Calendar.getInstance().apply { add(Calendar.MINUTE, bufferMinutes) }
        var hour = startHour
        var minute = 0
        while (hour < endHour || (hour == endHour && minute == 0)) {
            if (isToday) {
                val slotCal = Calendar.getInstance().apply {
                    timeInMillis = localMidnight
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (slotCal.after(bufferCal)) {
                    slots.add(String.format(Locale.US, "%02d:%02d", hour, minute))
                }
            } else {
                slots.add(String.format(Locale.US, "%02d:%02d", hour, minute))
            }
            minute += intervalMinutes
            if (minute >= 60) {
                minute -= 60
                hour++
            }
        }
        return slots
    }

    /** True when the selected local date+time is in the future by at least [bufferMinutes]. */
    fun isFutureDateTime(dateEpochMs: Long, time: String, bufferMinutes: Int = 30): Boolean {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return false
        val localMidnight = datePickerMillisToLocalMidnight(dateEpochMs)
        val cal = Calendar.getInstance().apply {
            timeInMillis = localMidnight
            set(Calendar.HOUR_OF_DAY, parts[0])
            set(Calendar.MINUTE, parts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val bufferCal = Calendar.getInstance().apply { add(Calendar.MINUTE, bufferMinutes) }
        return cal.after(bufferCal)
    }
}
