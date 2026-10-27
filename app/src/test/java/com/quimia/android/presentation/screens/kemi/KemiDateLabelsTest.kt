package com.quimia.android.presentation.screens.kemi

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

class KemiDateLabelsTest {
    private val zone = TimeZone.getTimeZone("America/Sao_Paulo")
    private fun time(value: String) = SimpleDateFormat("yyyy-MM-dd HH:mm").apply { timeZone = zone }.parse(value)!!.time

    @Test fun datesUseCalendarDaysRatherThanTwentyFourHourDurations() {
        val now = time("2026-10-26 00:01")
        assertEquals("Hoje", kemiHistoryDate(now, now, zone))
        assertEquals("Ontem", kemiHistoryDate(time("2026-10-25 23:59"), now, zone))
        assertEquals("Há 3 dias", kemiHistoryDate(time("2026-10-23 12:00"), now, zone))
    }

    @Test fun olderDatesAndYearTransitionsAreUnambiguous() {
        assertEquals("01/10/2026", kemiHistoryDate(time("2026-10-01 12:00"), time("2026-10-26 12:00"), zone))
        assertEquals("Ontem", kemiHistoryDate(time("2025-12-31 23:50"), time("2026-01-01 00:10"), zone))
    }

    @Test fun recentMessagesAreLabelledNow() {
        assertEquals("Agora", kemiMessageTime(100_000, 110_000))
    }
}
