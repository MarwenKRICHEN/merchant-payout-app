package com.example.androidinterview.payout.ui

private const val VISIBLE_PREFIX = 4
private const val VISIBLE_SUFFIX = 4
private const val MASK_CHAR = '*'

/**
 * Hides the middle of an IBAN for display, e.g. `GB29NWBK60161331926819` → `GB29**************6819`.
 *
 * Returned unchanged when too short to be masked.
 */
fun String.maskIban(): String {
    if (length <= VISIBLE_PREFIX + VISIBLE_SUFFIX) return this
    return take(VISIBLE_PREFIX) + MASK_CHAR.toString().repeat(length - VISIBLE_PREFIX - VISIBLE_SUFFIX) + takeLast(VISIBLE_SUFFIX)
}
