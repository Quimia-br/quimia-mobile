package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class BirthDateTest {
    private val today = BirthDate(2026, 10, 26)

    @Test fun manualInputFormatsDigitsAndAcceptsPastedDatesWithoutMoreThanEightDigits() {
        assertEquals("", formatBirthDate("abc"))
        assertEquals("14", formatBirthDate("14"))
        assertEquals("14/1", formatBirthDate("141"))
        assertEquals("14/11", formatBirthDate("1411"))
        assertEquals("14/11/1995", formatBirthDate("14/11/1995"))
        assertEquals("14/11/1995", formatBirthDate("14111995000"))
    }

    @Test fun everyCaretPositionRoundTripsThroughTheDateMaskIncludingPartialDates() {
        for (length in 0..8) {
            val digits = "14111995".take(length)
            val result = BirthDateVisualTransformation.filter(androidx.compose.ui.text.AnnotatedString(digits))
            for (offset in 0..length) {
                val transformed = result.offsetMapping.originalToTransformed(offset)
                assertTrue(transformed in 0..result.text.length)
                assertEquals(offset, result.offsetMapping.transformedToOriginal(transformed))
            }
            for (offset in 0..result.text.length) assertTrue(result.offsetMapping.transformedToOriginal(offset) in 0..length)
        }
    }

    @Test fun birthYearStartsAt1900InAllFormsAndSerialization() {
        listOf("01/01/0099", "31/12/1899").forEach {
            assertNull(BirthDate.parse(it))
            assertNotNull(completeBirthDateError(it, today))
            assertNotNull(com.quimia.android.presentation.screens.auth.authRegistrationBirthDateError(it, today))
            assertTrue(runCatching { com.quimia.android.presentation.screens.auth.authApiBirthDate(it) }.isFailure)
            assertTrue(runCatching { com.quimia.android.presentation.screens.profile.profileIsoDate(it) }.isFailure)
        }
        assertEquals("1900-01-01", com.quimia.android.presentation.screens.auth.authApiBirthDate("01/01/1900"))
        assertNull(completeBirthDateError("01/01/1900", today))
    }

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
