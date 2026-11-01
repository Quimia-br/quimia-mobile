package com.quimia.android.presentation.components

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

internal const val MinimumBirthYear = 1900

internal fun formatBirthDate(value: String): String {
    val digits = value.filter { it in '0'..'9' }.take(8)
    return buildString {
        digits.forEachIndexed { index, digit ->
            if (index == 2 || index == 4) append('/')
            append(digit)
        }
    }
}

internal data class BirthDate(val year: Int, val month: Int, val day: Int) : Comparable<BirthDate> {
    override fun compareTo(other: BirthDate): Int = compareValuesBy(this, other, { it.year }, { it.month }, { it.day })
    override fun toString(): String = String.format(Locale.ROOT, "%02d/%02d/%04d", day, month, year)

    companion object {
        fun today(): BirthDate = Calendar.getInstance().let { BirthDate(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH)) }
        fun parse(value: String): BirthDate? {
            if (!value.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) return null
            val day = value.take(2).toInt()
            val month = value.substring(3, 5).toInt()
            val year = value.takeLast(4).toInt()
            if (year !in MinimumBirthYear..9999 || month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
            return BirthDate(year, month, day)
        }
    }
}

internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

internal fun birthCalendarDays(year: Int, month: Int): List<Int?> {
    val first = GregorianCalendar().apply {
        gregorianChange = java.util.Date(Long.MIN_VALUE)
        clear()
        set(year, month - 1, 1)
    }.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    return List(42) { index -> (index - first + 1).takeIf { it in 1..daysInMonth(year, month) } }
}

internal fun isLegacyBirthDate(value: String): Boolean =
    value.matches(Regex("\\d{2}/\\d{2}")) && BirthDate.parse("$value/2000") != null

internal fun completeBirthDateError(value: String, today: BirthDate = BirthDate.today()): String? {
    if (value.isBlank()) return null
    if (isLegacyBirthDate(value)) return "Selecione também o ano de nascimento."
    if (value.matches(Regex("\\d{2}/\\d{2}/\\d{4}")) && value.takeLast(4).toInt() < MinimumBirthYear)
        return "O ano de nascimento deve ser 1900 ou posterior."
    val date = BirthDate.parse(value) ?: return "Informe uma data válida no formato DD/MM/AAAA."
    return if (date > today) "A data de nascimento não pode estar no futuro." else null
}
