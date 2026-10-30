package ir.ehsananish.dstest

import org.junit.Assert.*
import org.junit.Test

class LogicTest {
    private fun e(day: Long, mood: Int, vararg tags: Int) = Entry(day, mood, tags.toSet())

    @Test fun validMood() { assertTrue(Logic.validMood(1)); assertTrue(Logic.validMood(5)); assertFalse(Logic.validMood(0)); assertFalse(Logic.validMood(6)) }

    @Test fun streakCounts() { assertEquals(3, Logic.streak(setOf(10L, 9L, 8L, 5L), 10L)) }
    @Test fun streakTodayMissing() { assertEquals(2, Logic.streak(setOf(9L, 8L), 10L)) }
    @Test fun streakBreaks() { assertEquals(0, Logic.streak(setOf(7L), 10L)); assertEquals(0, Logic.streak(emptySet(), 10L)) }
    @Test fun longest() { assertEquals(3, Logic.longestStreak(setOf(1L, 2L, 3L, 7L, 8L))); assertEquals(0, Logic.longestStreak(emptySet())) }

    @Test fun averageOf() { assertEquals(3.0, Logic.average(listOf(e(1, 2), e(2, 4)))!!, 1e-9); assertNull(Logic.average(emptyList())) }

    @Test fun weekSentence() {
        val prev = (0L..6L).map { e(100 + it, 2) }
        val cur = (7L..13L).map { e(100 + it, 4) }
        assertTrue(Logic.weekSentence(prev + cur, 113).contains("بهتر"))
        assertTrue(Logic.weekSentence(emptyList(), 113).contains("چند روز"))
        val worse = (0L..6L).map { e(100 + it, 5) } + (7L..13L).map { e(100 + it, 3) }
        assertTrue(Logic.weekSentence(worse, 113).contains("پایین"))
    }

    @Test fun weekdayAverages() {
        // epoch day 2 = Saturday (day 0 was Thursday)
        val sat = 2L
        assertEquals(0, Logic.satOffset(sat))
        val avg = Logic.weekdayAverages(listOf(e(sat, 2), e(sat + 7, 4), e(sat + 1, 5)))
        assertEquals(3.0, avg[0]!!, 1e-9); assertEquals(5.0, avg[1]!!, 1e-9); assertNull(avg[2])
    }

    @Test fun weekStartIsSaturday() { assertEquals(2L, Logic.weekStart(2L)); assertEquals(2L, Logic.weekStart(8L)); assertEquals(9L, Logic.weekStart(9L)) }

    @Test fun tagImpactSign() {
        val list = (0L..5L).map { e(it, 5, 4) } + (6L..11L).map { e(it, 2) }
        val imp = Logic.tagImpacts(list)
        assertEquals(1, imp.size)
        assertEquals(4, imp[0].tag)
        assertEquals(3.0, imp[0].diff, 1e-9)
    }
    @Test fun tagImpactMinSamples() { assertTrue(Logic.tagImpacts(listOf(e(1, 5, 0), e(2, 5, 0), e(3, 1), e(4, 1), e(5, 1))).isEmpty()) }

    @Test fun searchMatches() {
        val list = listOf(Entry(1, 4, setOf(4), "امروز ۱۲ دقیقه دویدم"), Entry(2, 2, emptySet(), "خسته", listOf("چای")))
        assertEquals(1, Logic.search(list, "12").size)
        assertEquals(1, Logic.search(list, "٢").size)
        assertEquals(1, Logic.search(list, "ورزش").size)
        assertEquals(1, Logic.search(list, "چای").size)
        assertEquals(2, Logic.search(list, "").size)
        assertEquals(1, Logic.search(list, "", mood = 2).size)
        assertEquals(1, Logic.search(list, "", tag = 4).size)
    }

    @Test fun pinHash() {
        assertEquals(Logic.pinHash("1234"), Logic.pinHash("۱۲۳۴"))
        assertNotEquals(Logic.pinHash("1234"), Logic.pinHash("1235"))
    }

    @Test fun nextReminder() {
        val tz = 12_600_000L // +03:30
        val midnightUtc = 0L
        // local time 03:30, reminder 21:00 → same local day
        val n = Logic.nextReminder(midnightUtc, tz, 21 * 60)
        assertEquals(21 * 3600_000L - tz, n)
        // reminder already passed (01:00 local) → next day
        val n2 = Logic.nextReminder(midnightUtc, tz, 60)
        assertEquals(86_400_000L + 3600_000L - tz, n2)
        assertTrue(n2 > midnightUtc)
    }

    @Test fun promptStable() { assertEquals(Logic.promptFor(5), Logic.promptFor(5)); assertNotEquals(Logic.promptFor(5), Logic.promptFor(6)); assertTrue(Logic.promptFor(-3).isNotEmpty()) }
    @Test fun hhmm() { assertEquals("۰۹:۰۵", Logic.hhmm(545)) }

    @Test fun backupRoundTrip() {
        val list = listOf(Entry(10, 4, setOf(1, 5), "یادداشت", listOf("الف", "ب")), Entry(11, 2))
        val back = Codec.fromText(Codec.toText(list))!!
        assertEquals(list, back)
    }
    @Test fun backupRejectsOthers() { assertNull(Codec.fromText("{\"magic\":\"x\"}")); assertNull(Codec.fromText("hello")); assertNull(Codec.fromText(null)) }
    @Test fun backupSkipsBad() {
        val t = "{\"magic\":\"halnameh-backup\",\"v\":1,\"e\":[{\"d\":1,\"m\":9},{\"d\":2,\"m\":3,\"t\":[99,2]},{\"x\":1},{\"d\":2,\"m\":5}]}"
        val back = Codec.fromText(t)!!
        assertEquals(1, back.size)
        assertEquals(setOf(2), back[0].tags)
    }
}

class JalaliTest {
    @Test fun monthLengths() {
        assertEquals(31, jalaliMonthLength(1404, 1)); assertEquals(30, jalaliMonthLength(1404, 7)); assertEquals(29, jalaliMonthLength(1404, 12))
        assertEquals(30, jalaliMonthLength(1403, 12)) // leap year
    }
    @Test fun roundTrip() {
        for (e in listOf(20000L, 20500L, 21000L)) { val j = jalali(e); assertEquals(e, jalaliToEpoch(j[0], j[1], j[2])) }
    }
    @Test fun knownDate() { assertEquals(listOf(1403, 1, 1), jalaliToEpochDay(1403, 1, 1).let { jalali(it).toList() }) }
    private fun jalaliToEpochDay(y: Int, m: Int, d: Int) = jalaliToEpoch(y, m, d)
}
