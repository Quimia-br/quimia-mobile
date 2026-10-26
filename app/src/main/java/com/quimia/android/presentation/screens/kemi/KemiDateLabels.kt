package com.quimia.android.presentation.screens.kemi

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun kemiHistoryDate(timestamp: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val date = Calendar.getInstance(zone).apply { timeInMillis = timestamp }
    val day = Calendar.getInstance(zone).apply { timeInMillis = now }
    for (daysAgo in 0..6) {
        if (date.get(Calendar.ERA) == day.get(Calendar.ERA) && date.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
            date.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)) {
            return when (daysAgo) { 0 -> "Hoje"; 1 -> "Ontem"; else -> "Há $daysAgo dias" }
        }
        day.add(Calendar.DAY_OF_YEAR, -1)
    }
    return SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply { timeZone = zone }.format(Date(timestamp))
}

internal fun kemiMessageTime(timestamp: Long, now: Long): String =
    if (now - timestamp in 0..59_999L) "Agora"
    else SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(timestamp))
