package com.crm.enhance_wellness.feature.notifications.domain.model

/**
 * Per-device notification preferences.
 *
 * Stored locally rather than on the server: the backend has no notification-preference
 * endpoint, and these are device-scoped anyway (a patient may want push on their phone
 * but not on a tablet). [com.crm.enhance_wellness.core.websocket.WellnessSocketManager]
 * consults these before raising a system notification.
 */
data class NotificationPreferences(
    val enabledCategories: Set<String> = ALL_CATEGORIES,
    val enabledChannels: Set<String> = ALL_CHANNELS,
    val quietStart: String = DEFAULT_QUIET_START,
    val quietEnd: String = DEFAULT_QUIET_END,
) {
    fun isCategoryEnabled(key: String): Boolean = key in enabledCategories

    fun isChannelEnabled(key: String): Boolean = key in enabledChannels

    /**
     * True when [minuteOfDay] falls inside the quiet window. The window wraps midnight
     * whenever start is later than end (the common 22:00 → 07:00 case), which is why this
     * is not a simple `in start..end` range check.
     */
    fun isQuietAt(minuteOfDay: Int): Boolean {
        val start = parseMinutes(quietStart) ?: return false
        val end = parseMinutes(quietEnd) ?: return false
        if (start == end) return false
        return if (start < end) minuteOfDay in start until end
        else minuteOfDay >= start || minuteOfDay < end
    }

    companion object {
        const val CATEGORY_APPOINTMENTS = "appointment_reminders"
        const val CATEGORY_PRESCRIPTIONS = "prescription_ready"
        const val CATEGORY_PAYMENTS = "payment_receipts"
        const val CATEGORY_MEMBERSHIPS = "membership_updates"
        const val CATEGORY_GIFT_CARDS = "gift_card_activity"

        const val CHANNEL_IN_APP = "in_app"
        const val CHANNEL_PUSH = "push"
        const val CHANNEL_EMAIL = "email"

        const val DEFAULT_QUIET_START = "22:00"
        const val DEFAULT_QUIET_END = "07:00"

        val ALL_CATEGORIES = setOf(
            CATEGORY_APPOINTMENTS,
            CATEGORY_PRESCRIPTIONS,
            CATEGORY_PAYMENTS,
            CATEGORY_MEMBERSHIPS,
            CATEGORY_GIFT_CARDS,
        )

        val ALL_CHANNELS = setOf(CHANNEL_IN_APP, CHANNEL_PUSH, CHANNEL_EMAIL)

        /** "HH:mm" → minutes since midnight; null when malformed. */
        fun parseMinutes(value: String): Int? {
            val parts = value.split(':')
            if (parts.size != 2) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            if (hour !in 0..23 || minute !in 0..59) return null
            return hour * 60 + minute
        }
    }
}
