package com.example.androidinterview.payout.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class IbanMaskTest {

    @Test
    fun `keeps the first and last four characters`() {
        assertEquals("GB29**************6819", "GB29NWBK60161331926819".maskIban())
    }

    @Test
    fun `leaves short values unchanged`() {
        assertEquals("GB29", "GB29".maskIban())
        assertEquals("GB296819", "GB296819".maskIban())
    }
}
