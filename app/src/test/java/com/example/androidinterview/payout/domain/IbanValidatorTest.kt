package com.example.androidinterview.payout.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IbanValidatorTest {

    private val validator = IbanValidator()

    @Test
    fun `accepts well-formed IBANs with a valid checksum`() {
        assertTrue(validator.isValid("GB29NWBK60161331926819"))
        assertTrue(validator.isValid("DE89370400440532013000"))
        assertTrue(validator.isValid("FR1420041010050500013M02606"))
    }

    @Test
    fun `rejects the malformed example from the challenge`() {
        assertFalse(validator.isValid("FR1212345123451234567A12310131231231231"))
    }

    @Test
    fun `rejects a wrong checksum`() {
        assertFalse(validator.isValid("GB28NWBK60161331926819"))
    }

    @Test
    fun `rejects too short, too long and non-alphanumeric input`() {
        assertFalse(validator.isValid(""))
        assertFalse(validator.isValid("GB29NWBK"))
        assertFalse(validator.isValid("GB29NWBK601613319268190000000000000000000"))
        assertFalse(validator.isValid("GB29-NWBK-6016-1331-9268-19"))
    }

    @Test
    fun `normalize strips whitespace and upper-cases`() {
        assertEquals("GB29NWBK60161331926819", validator.normalize(" gb29 nwbk 6016 1331 9268 19 "))
    }
}
