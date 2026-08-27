package com.crm.enhance_wellness.feature.notifications.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NotificationPreferencesTest {

    private fun at(hour: Int, minute: Int = 0) = hour * 60 + minute

    @Test
    fun `defaults enable every category and channel`() {
        val prefs = NotificationPreferences()
        NotificationPreferences.ALL_CATEGORIES.forEach {
            assertTrue(prefs.isCategoryEnabled(it), "category $it should default to enabled")
        }
        NotificationPreferences.ALL_CHANNELS.forEach {
            assertTrue(prefs.isChannelEnabled(it), "channel $it should default to enabled")
        }
    }

    @Test
    fun `disabled category reports as disabled`() {
        val prefs = NotificationPreferences(
            enabledCategories = NotificationPreferences.ALL_CATEGORIES -
                NotificationPreferences.CATEGORY_GIFT_CARDS,
        )
        assertFalse(prefs.isCategoryEnabled(NotificationPreferences.CATEGORY_GIFT_CARDS))
        assertTrue(prefs.isCategoryEnabled(NotificationPreferences.CATEGORY_APPOINTMENTS))
    }

    // The default window (22:00–07:00) wraps midnight, which a naive range check gets wrong.
    @Test
    fun `quiet window wrapping midnight covers both sides of midnight`() {
        val prefs = NotificationPreferences(quietStart = "22:00", quietEnd = "07:00")

        assertTrue(prefs.isQuietAt(at(22)), "22:00 is the start of the window")
        assertTrue(prefs.isQuietAt(at(23, 59)))
        assertTrue(prefs.isQuietAt(at(0)))
        assertTrue(prefs.isQuietAt(at(6, 59)))

        assertFalse(prefs.isQuietAt(at(7)), "07:00 is the exclusive end")
        assertFalse(prefs.isQuietAt(at(12)))
        assertFalse(prefs.isQuietAt(at(21, 59)))
    }

    @Test
    fun `quiet window inside a single day does not wrap`() {
        val prefs = NotificationPreferences(quietStart = "09:00", quietEnd = "17:00")

        assertFalse(prefs.isQuietAt(at(8, 59)))
        assertTrue(prefs.isQuietAt(at(9)))
        assertTrue(prefs.isQuietAt(at(16, 59)))
        assertFalse(prefs.isQuietAt(at(17)))
        assertFalse(prefs.isQuietAt(at(23)))
    }

    @Test
    fun `equal start and end means quiet hours are off, not always-on`() {
        val prefs = NotificationPreferences(quietStart = "08:00", quietEnd = "08:00")
        assertFalse(prefs.isQuietAt(at(8)))
        assertFalse(prefs.isQuietAt(at(3)))
    }

    @Test
    fun `malformed quiet times disable quiet hours rather than muting everything`() {
        assertFalse(NotificationPreferences(quietStart = "oops", quietEnd = "07:00").isQuietAt(at(2)))
        assertFalse(NotificationPreferences(quietStart = "22:00", quietEnd = "25:00").isQuietAt(at(23)))
        assertFalse(NotificationPreferences(quietStart = "22", quietEnd = "07:00").isQuietAt(at(23)))
    }

    @Test
    fun `parseMinutes accepts valid times and rejects invalid ones`() {
        assertEquals(0, NotificationPreferences.parseMinutes("00:00"))
        assertEquals(23 * 60 + 59, NotificationPreferences.parseMinutes("23:59"))
        assertNull(NotificationPreferences.parseMinutes("24:00"))
        assertNull(NotificationPreferences.parseMinutes("12:60"))
        assertNull(NotificationPreferences.parseMinutes("12"))
        assertNull(NotificationPreferences.parseMinutes(""))
    }
}
