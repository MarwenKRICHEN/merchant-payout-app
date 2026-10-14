package com.example.androidinterview.payout.domain

import javax.inject.Inject

private val IBAN_FORMAT = Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$")
private const val IBAN_CHECK_MODULUS = 97
private const val IBAN_VALID_REMAINDER = 1
private const val IBAN_HEADER_LENGTH = 4
private const val LETTER_OFFSET = 10

/** Normalises and validates the destination IBAN of a payout. */
class IbanValidator @Inject constructor() {

    /**
     * Removes whitespace and upper-cases [input], the form in which an IBAN is validated and sent.
     *
     * @param input IBAN as typed by the user.
     */
    fun normalize(input: String): String = input.filterNot { it.isWhitespace() }.uppercase()

    /**
     * Whether [iban] is a well-formed IBAN: country code, check digits, 11–30 alphanumerics and a
     * valid ISO 13616 mod-97 checksum.
     *
     * @param iban Normalised IBAN, see [normalize].
     */
    fun isValid(iban: String): Boolean {
        if (!IBAN_FORMAT.matches(iban)) return false
        val rearranged = iban.substring(IBAN_HEADER_LENGTH) + iban.substring(0, IBAN_HEADER_LENGTH)
        var remainder = 0
        for (char in rearranged) {
            val digits = if (char.isDigit()) char.digitToInt().toString() else (char - 'A' + LETTER_OFFSET).toString()
            for (digit in digits) {
                remainder = (remainder * 10 + digit.digitToInt()) % IBAN_CHECK_MODULUS
            }
        }
        return remainder == IBAN_VALID_REMAINDER
    }
}
