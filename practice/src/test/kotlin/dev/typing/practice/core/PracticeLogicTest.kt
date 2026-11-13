package dev.typing.practice.core

import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer
import java.util.Random
import java.util.function.LongSupplier

class PracticeLogicTest {
    @Test fun expandedEnglishPromptsAreUniqueAndCycle() {
        for ((mode, minimum) in listOf("단어" to 50, "문장" to 30)) {
            val size = PromptRepository.size("English", mode)
            assertTrue(size >= minimum)
            val prompts = (0 until size).map { PromptRepository.get("English", mode, it) }
            assertEquals(size, prompts.toSet().size)
            assertTrue(prompts.all { it.isNotBlank() })
            assertEquals(prompts.first(), PromptRepository.get("English", mode, size))
        }
    }
    private class Clock : LongSupplier {
        var now = 0L
        var reads = 0
        override fun getAsLong(): Long { reads++; return now }
    }

    private fun replace(session: PracticeSession, old: String, next: String) {
        session.beforeChange(old, 0, old.length)
        session.afterChange(next, 0, next.length)
        assertEquals(TypingUnits.count(next), session.units)
    }

    @Test fun unitExamplesAndAllModernSyllables() {
        val cases = mapOf("" to 0, "가" to 2, "한글" to 6, "과" to 3, "괜" to 4,
            "값" to 4, "읽" to 4, "까" to 2, "ㄱㅘㄳ" to 5, "한글" to 6,
            "과" to 3, "ㅙㅞㅢ" to 6, "ㅐㅒㅔㅖ" to 4, "Hello, world!" to 13,
            "한 글!" to 8, "\n\t😀" to 0)
        cases.forEach { (text, expected) -> assertEquals(text, expected, TypingUnits.count(text)) }
        for (c in 0xac00..0xd7a3) {
            val composed = c.toChar().toString()
            assertEquals(TypingUnits.count(composed), TypingUnits.count(Normalizer.normalize(composed, Normalizer.Form.NFD)))
        }
        assertEquals(240, TypingUnits.perMinute(120, 30_000))
        assertEquals(0, TypingUnits.perMinute(1, 0))
    }

    @Test fun compositionCorrectionsAndPasteUseOnlyCurrentText() {
        val session = PracticeSession(Clock())
        var text = ""
        fun edit(start: Int, removed: Int, replacement: String, expected: Int) {
            session.beforeChange(text, start, removed)
            text = text.substring(0, start) + replacement + text.substring(start + removed)
            session.afterChange(text, start, replacement.length)
            assertEquals("input: $text", expected, session.units)
            assertEquals(expected, TypingUnits.count(text))
        }
        edit(0, 0, "ㅎ", 1)
        edit(0, 1, "하", 2)
        edit(0, 1, "한", 3)
        edit(1, 0, "글", 6)
        edit(0, 1, "달", 6)
        edit(1, 1, "", 3)
        edit(1, 0, " 과!", 8)
        edit(0, text.length, "Hello, world!", 13)
        edit(0, text.length, "", 0)
        edit(0, 0, "괎", 5)
        assertEquals(5, session.finish("괎", text).units)
    }
    @Test fun sessionClockCompositionAndFrozenResult() {
        val clock = Clock().apply { now = 10_000 }
        val session = PracticeSession(clock)
        session.resume()
        assertEquals(0L, session.elapsedMillis())
        replace(session, "", "ㄱ")
        clock.now += 500
        var old = "ㄱ"
        for (next in arrayOf("가", "각", "가", "")) { replace(session, old, next); old = next }
        assertEquals(0, session.units)
        assertEquals(500L, session.elapsedMillis())
        session.pause(); session.pause()
        clock.now += 60_000
        assertEquals(500L, session.elapsedMillis())
        session.resume(); session.resume()
        clock.now += 500
        assertEquals(1000L, session.elapsedMillis())
        replace(session, "", "한글")
        val result = session.finish("한글", "한글")
        assertEquals(6, result.units)
        assertEquals(360, result.perMinute)
        assertEquals(100, result.accuracy)
        clock.now += 20_000
        session.beforeChange("한글", 0, 2)
        session.afterChange("", 0, 0)
        assertEquals(6, session.units)
        assertEquals(1000L, session.elapsedMillis())
        assertSame(result, session.finish("different", "different"))
        session.reset()
        assertEquals(0, session.units)
        assertEquals(0L, session.elapsedMillis())
        assertEquals(0, session.finish("x", "").accuracy)
    }

    @Test fun restorationDistinguishesWaitingPausedAndFinished() {
        val clock = Clock().apply { now = 100_000 }
        val session = PracticeSession(clock)
        session.restore("", 1200, true, false)
        session.resume(); clock.now += 300
        assertEquals(1500L, session.elapsedMillis())
        session.restore("과", 2000, true, true)
        session.resume(); clock.now += 500
        assertEquals(3, session.units)
        assertEquals(2000L, session.elapsedMillis())
        session.restore("", 0, false, false)
        session.resume(); clock.now += 1000
        assertEquals(0L, session.elapsedMillis())
    }

    @Test fun randomMiddleEditsMatchFullCount() {
        val clock = Clock()
        val session = PracticeSession(clock)
        val random = Random(716)
        var text = ""
        val pieces = arrayOf("가", "ㄱ", "한", "값", "과", "까", " ", "!", "ab", "\n", "")
        repeat(3000) {
            val start = random.nextInt(text.length + 1)
            val removed = random.nextInt(text.length - start + 1)
            val inserted = pieces[random.nextInt(pieces.size)]
            session.beforeChange(text, start, removed)
            text = text.substring(0, start) + inserted + text.substring(start + removed)
            session.afterChange(text, start, inserted.length)
            assertEquals(TypingUnits.count(text), session.units)
            clock.now += 20
        }
        assertEquals(if (text.isEmpty()) 0 else 100, session.finish(text, text).accuracy)
    }

    @Test fun inputWorkStaysBounded() {
        val clock = Clock()
        val session = PracticeSession(clock)
        replace(session, "", "a")
        var reads = 0
        val text = object : CharSequence {
            override val length = 100_000
            override fun get(index: Int): Char { reads++; return 'a' }
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("substring in input path")
            override fun toString(): String = error("copy in input path")
        }
        val clockReads = clock.reads
        session.beforeChange(text, 50_000, 1)
        session.afterChange(text, 50_000, 1)
        assertEquals(2, reads)
        assertEquals(clockReads, clock.reads)
        assertEquals(0, FinalScore.calculate("abc", "abc", 3, 0).perMinute)
        assertEquals(67, FinalScore.calculate("abc", "ac", 2, 1000).accuracy)
    }

    @Test fun editAccuracyHandlesMissingExtraAndEquivalentHangul() {
        fun accuracy(a: String, b: String) = FinalScore.calculate(a, b, 0, 1000).accuracy
        assertEquals(80, accuracy("abcde", "abde"))
        assertEquals(83, accuracy("abcde", "abXcde"))
        assertEquals(80, accuracy("abcde", "abXde"))
        assertEquals(100, accuracy("한글", "한글"))
        assertEquals(0, accuracy("abc", ""))
        assertEquals(0, accuracy("", ""))
        assertEquals(100, accuracy("😀", "😀"))
    }
    @Test fun longTextWithSmallMiddleEditUsesEditDistance() {
        val prompt = "a".repeat(1000) + "b".repeat(1000)
        val input = "a".repeat(1000) + "x".repeat(100) + "b".repeat(1000)
        assertEquals(95, FinalScore.calculate(prompt, input, 0, 1000).accuracy)
        assertEquals(95, FinalScore.calculate(input, prompt, 0, 1000).accuracy)
    }
    @Test fun trimmedComparisonMatchesUntrimmedEditDistance() {
        val random = Random(451)
        repeat(500) {
            fun sample() = (0 until random.nextInt(1, 20)).map { "abc"[random.nextInt(3)] }.joinToString("")
            val a = sample()
            val b = sample()
            var previous = IntArray(b.length + 1) { it }
            for (i in a.indices) {
                val current = IntArray(b.length + 1)
                current[0] = i + 1
                for (j in b.indices) current[j + 1] = minOf(previous[j + 1] + 1, current[j] + 1,
                    previous[j] + if (a[i] == b[j]) 0 else 1)
                previous = current
            }
            val length = maxOf(a.length, b.length)
            val expected = kotlin.math.floor(100.0 * (length - previous[b.length]) / length + 0.5).toInt()
            assertEquals(expected, FinalScore.calculate(a, b, 0, 1000).accuracy)
        }
    }
    @Test fun sampleOrderIsUnchanged() {
        assertEquals("바람", PromptRepository.get("한국어", "단어", 0))
        assertEquals("바람", PromptRepository.get("한국어", "단어", PromptRepository.size("한국어", "단어")))
        assertEquals("Small steps make steady progress.", PromptRepository.get("English", "문장", 0))
        assertEquals(PromptRepository.get("English", "문단", 0), PromptRepository.get("English", "문단", 2))
    }

    @Test fun allBmpCharactersMatchLegacyRules() {
        fun vowel(i: Int) = if (i in intArrayOf(9, 10, 11, 14, 15, 16, 19)) 2 else 1
        fun tail(i: Int) = when (i) { 0 -> 0; 3, 5, 6, in 9..15, 18 -> 2; else -> 1 }
        for (c in 0..0xffff) {
            val expected = when (c) {
                in 0xac00..0xd7a3 -> { val n = c - 0xac00; 1 + vowel(n % 588 / 28) + tail(n % 28) }
                in 0x1161..0x1175 -> vowel(c - 0x1161)
                in 0x11a8..0x11c2 -> tail(c - 0x11a7)
                in 0x314f..0x3163 -> vowel(c - 0x314f)
                in 0x3131..0x314e -> if ("ㄳㄵㄶㄺㄻㄼㄽㄾㄿㅀㅄ".indexOf(c.toChar()) >= 0) 2 else 1
                in 0x1100..0x1112, in 0x20..0x7e -> 1
                else -> 0
            }
            assertEquals("U+${c.toString(16)}", expected, TypingUnits.count(c.toChar().toString()))
        }
    }

    @Test fun surrogateEditsStayAdditiveAndReadOnlyTheChangedRange() {
        val session = PracticeSession(Clock())
        val old = "a\uD83D\uDE00한"
        replace(session, "", old)
        // Replace just one half of a surrogate pair, as permitted by UTF-16 offsets.
        session.beforeChange(old, 1, 1)
        val next = "ab\uDE00한"
        session.afterChange(next, 1, 1)
        assertEquals(TypingUnits.count(next), session.units)
        val reads = mutableListOf<Int>()
        val guarded = object : CharSequence {
            override val length = old.length
            override fun get(index: Int): Char {
                check(index == 1) { "Read beyond changed range" }
                reads.add(index)
                return old[index]
            }
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("copy")
        }
        assertEquals(0, TypingUnits.count(guarded, 1, 2))
        assertEquals(listOf(1), reads)
    }
}




