package dev.typing.practice.core

import kotlin.math.roundToInt

/** Text-derived units, not physical IME touches. No collections in the input loop. */
object TypingUnits {
    // Direct primitive array lookups compile down to single BALOAD bytecode instructions,
    // eliminating method dispatch frames and branch tableswitch/lookupswitch opcodes.
    private val VOWEL_WEIGHTS = byteArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1)
    private val TAIL_WEIGHTS = byteArrayOf(
        0, 1, 1, 2, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    )

    fun count(text: CharSequence, start: Int = 0, end: Int = text.length): Int {
        var total = 0
        var i = start
        while (i < end) {
            val c = text[i++].code
            total += when (c) {
                in 0x20..0x7e -> 1
                in 0xac00..0xd7a3 -> {
                    val index = c - 0xac00
                    1 + VOWEL_WEIGHTS[index % 588 / 28].toInt() + TAIL_WEIGHTS[index % 28].toInt()
                }
                in 0x1161..0x1175 -> VOWEL_WEIGHTS[c - 0x1161].toInt()
                in 0x11a8..0x11c2 -> TAIL_WEIGHTS[c - 0x11a7].toInt()
                in 0x314f..0x3163 -> VOWEL_WEIGHTS[c - 0x314f].toInt()
                in 0x3131..0x314e -> when (c) {
                    0x3133, 0x3135, 0x3136, in 0x313a..0x3140, 0x3144 -> 2
                    else -> 1
                }
                in 0x1100..0x1112 -> 1
                else -> 0
            }
        }
        return total
    }

    fun perMinute(units: Int, millis: Long): Int =
        if (millis <= 0) 0 else (units * 60_000.0 / millis).roundToInt()
}
