package dev.typing.practice

import dev.typing.practice.core.*
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale
import java.util.Random
import java.util.function.LongSupplier

class AppLogicTest {
    private class Clock : LongSupplier {
        var now = 0L
        override fun getAsLong() = now
    }

    @Test fun historySelectionFollowsPaneChanges() {
        assertEquals("history", HistoryLayoutPolicy.screenAfterResize("historyDetail", "id", false, true))
        assertEquals("historyDetail", HistoryLayoutPolicy.screenAfterResize("history", "id", true, false))
        assertEquals("history", HistoryLayoutPolicy.screenAfterResize("history", "", true, false))
        assertEquals("history", HistoryLayoutPolicy.screenAfterResize("history", "id", true, true))
        assertEquals("result", HistoryLayoutPolicy.screenAfterResize("result", "id", false, true))
    }
    @Test fun formatterReusesUnchangedText() {
        val formatter = MetricsFormatter("한국어", "문장")
        val first = formatter.format(0, 0)
        assertSame(first, formatter.format(0, 200))
        assertTrue(formatter.format(2, 200).contains("2타"))
        assertTrue(formatter.format(2, 1000).contains("120타/분"))
        val later = formatter.format(10, 10000)
        assertSame(later, formatter.format(10, 10001))
    }

    @Test fun compactMetricsOmitsRawUnitsAndReusesVisibleValues() {
        val formatter = MetricsFormatter("한국어", "문단", compact = true)
        val first = formatter.format(0, 0)
        assertEquals("한국어 · 문단 | 0초 · 측정 중", first)
        assertSame(first, formatter.format(30, 500))
        assertEquals("한국어 · 문단 | 1초 · 120타/분", formatter.format(2, 1000))
    }

    @Test fun kotlinMatchesCapturedJava09Results() {
        val digest = MessageDigest.getInstance("SHA-256")
        fun append(line: String) = digest.update(line.toByteArray(Charsets.UTF_8))
        for (c in 0xac00..0xd7a3) {
            val s = c.toChar().toString()
            append("$c:${TypingUnits.count(s)}:${TypingUnits.count(Normalizer.normalize(s, Normalizer.Form.NFD))}\n")
        }
        val random = Random(716)
        val pieces = arrayOf("가", "ㄱ", "한", "값", "과", "까", " ", "!", "ab", "\n", "")
        val clock = Clock()
        val session = PracticeSession(clock)
        val formatter = MetricsFormatter("한국어", "문장")
        var text = ""
        repeat(3000) { i ->
            val start = random.nextInt(text.length + 1)
            val removed = random.nextInt(text.length - start + 1)
            val inserted = pieces[random.nextInt(pieces.size)]
            session.beforeChange(text, start, removed)
            text = text.substring(0, start) + inserted + text.substring(start + removed)
            session.afterChange(text, start, inserted.length)
            if (i % 11 == 0) { session.pause(); clock.now += 5000; session.resume() }
            clock.now += 20
            append("${session.state}:${session.units}:${session.elapsedMillis()}:${formatter.format(session.units, session.elapsedMillis())}\n")
        }
        val result = session.finish(text, text)
        append("${result.units}:${result.elapsedMillis}:${result.perMinute}:${result.accuracy}\n")
        val actual = digest.digest().joinToString("") { String.format(Locale.ROOT, "%02x", it.toInt() and 255) }
        val expected = requireNotNull(javaClass.getResourceAsStream("/java09-golden.sha256")).bufferedReader().use { it.readText().trim() }
        assertEquals(expected, actual)
    }

}

