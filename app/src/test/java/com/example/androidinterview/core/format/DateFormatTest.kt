package com.example.androidinterview.core.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateFormatTest {

    @Test
    fun `formats day, abbreviated month and year`() {
        assertEquals("23 Jan 2026", LocalDate.of(2026, 1, 23).formatShort(Locale.UK))
    }

    @Test
    fun `pads single-digit days`() {
        assertEquals("05 May 2026", LocalDate.of(2026, 5, 5).formatShort(Locale.UK))
    }
}
