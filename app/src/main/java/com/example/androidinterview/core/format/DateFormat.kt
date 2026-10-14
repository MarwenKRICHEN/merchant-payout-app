package com.example.androidinterview.core.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats the date as day, abbreviated month and year, e.g. `23 Jan 2026`.
 *
 * @param locale Locale used for the month name.
 */
fun LocalDate.formatShort(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))
