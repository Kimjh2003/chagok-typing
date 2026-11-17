package dev.typing.practice.core

/** Additive UTF-16 range counts: supports edits without rescanning input. */
fun interface TypingUnitPolicy {
    fun count(text: CharSequence, start: Int, end: Int): Int
}

object KoreanTypingUnitPolicy : TypingUnitPolicy {
    override fun count(text: CharSequence, start: Int, end: Int): Int = TypingUnits.count(text, start, end)
}

object EnglishTypingUnitPolicy : TypingUnitPolicy {
    override fun count(text: CharSequence, start: Int, end: Int): Int {
        var total = 0
        for (i in start until end) if (text[i] in ' '..'~') total++
        return total
    }
}
