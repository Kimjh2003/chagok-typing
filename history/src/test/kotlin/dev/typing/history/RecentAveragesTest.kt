package dev.typing.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecentAveragesTest {
    @Test fun emptyRecordsHaveNoAverage() {
        assertNull(recentAverages(emptyList()))
    }

    @Test fun onlyNewestSevenContribute() {
        val records = (1..8).map { HistorySummary("$it", it.toLong(), "문장", it * 10, it * 10) }
        assertEquals(RecentAverages(7, 40, 40), recentAverages(records))
    }

    @Test fun partialHistoryIsAveraged() {
        val records = listOf(HistorySummary("a", 2, "단어", 200, 90), HistorySummary("b", 1, "단어", 100, 80))
        assertEquals(RecentAverages(2, 150, 85), recentAverages(records))
    }
}
