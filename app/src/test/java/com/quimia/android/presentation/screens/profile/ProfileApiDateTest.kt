package com.quimia.android.presentation.screens.profile

import org.junit.Assert.*
import org.junit.Test

class ProfileApiDateTest {
    @Test fun `date sends year month day without timezone conversion`() {
        assertEquals("2000-02-29", profileIsoDate("29/02/2000"))
        assertEquals("29/02/2000", profileDisplayDate("2000-02-29"))
    }
    @Test fun `missing year and impossible dates never reach the backend`() {
        listOf("", "20/04", "31/02/2000", "01/01/9999").forEach {
            assertTrue(runCatching { profileIsoDate(it) }.isFailure)
        }
    }
}
