package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class BirthDateTest {
    private val today = BirthDate(2026, 10, 26)

    @Test fun rejectsImpossibleDatesAndUsesGregorianLeapYears() {
        listOf("29/02/2001", "29/02/1900", "31/04/2000", "00/01/2000", "01/00/2000", "01/01/0000", "01/01", "1/1/2000")
            .forEach { assertNull(it, BirthDate.parse(it)) }
        listOf("29/02/2000", "29/02/2024", "31/12/1990").forEach { assertEquals(it, BirthDate.parse(it)?.toString()) }
    }

    @Test fun todayIsAllowedButTomorrowAndFutureYearsAreNot() {
        assertNull(completeBirthDateError("26/10/2026", today))
        assertNotNull(completeBirthDateError("27/10/2026", today))
        assertNotNull(completeBirthDateError("01/01/2027", today))
    }

    @Test fun legacyDayMonthIsPreservedButRequiresChoosingAYear() {
        assertTrue(isLegacyBirthDate("20/04"))
        assertTrue(isLegacyBirthDate("29/02"))
        assertFalse(isLegacyBirthDate("31/04"))
        assertNotNull(completeBirthDateError("20/04", today))
        assertNull(BirthDate.parse("20/04"))
    }

    @Test fun calendarStartsOnSundayHasSixWeeksAndHandlesLeapFebruary() {
        val october = birthCalendarDays(2026, 10)
        assertEquals(42, october.size)
        assertEquals(1, october[4])
        assertEquals(31, october.filterNotNull().size)
        assertEquals(29, birthCalendarDays(2024, 2).filterNotNull().size)
        assertEquals(28, birthCalendarDays(2025, 2).filterNotNull().size)
        assertEquals(1, birthCalendarDays(2026, 2).first())
    }
}
