package ir.ehsananish.dstest

import ir.ehsananish.dstest.ds.fa

val JALALI_MONTHS = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

// Indexed from Sunday (epoch day 0 was a Thursday).
private val WEEKDAYS = listOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
val WEEKDAY_SHORT = listOf("ی", "د", "س", "چ", "پ", "ج", "ش")

fun weekdayIndex(epochDay: Long): Int = Math.floorMod(epochDay + 4, 7L).toInt()

fun weekdayName(epochDay: Long): String = WEEKDAYS[weekdayIndex(epochDay)]
fun weekdayShort(epochDay: Long): String = WEEKDAY_SHORT[weekdayIndex(epochDay)]

private fun civilFromDays(days: Long): IntArray {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    var y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    if (m <= 2) y += 1
    return intArrayOf(y.toInt(), m.toInt(), d.toInt())
}

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
    val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    val gy2 = if (gm > 2) gy + 1 else gy
    var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
    var jy = -1595 + 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
    val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
    return intArrayOf(jy, jm, jd)
}

fun jalali(epochDay: Long): IntArray {
    val g = civilFromDays(epochDay)
    return gregorianToJalali(g[0], g[1], g[2])
}

fun jalaliLong(epochDay: Long): String {
    val j = jalali(epochDay)
    return "${weekdayName(epochDay)}، ${j[2].fa()} ${JALALI_MONTHS[j[1] - 1]} ${j[0].fa()}"
}

fun jalaliShort(epochDay: Long): String {
    val j = jalali(epochDay)
    return "${j[2].fa()} ${JALALI_MONTHS[j[1] - 1]}"
}

private fun cmp(a: IntArray, jy: Int, jm: Int, jd: Int): Int =
    when { a[0] != jy -> a[0].compareTo(jy); a[1] != jm -> a[1].compareTo(jm); else -> a[2].compareTo(jd) }

/** Epoch day of a Jalali date (day may overflow the month; use [startOfMonth] + day - 1 for clamped dates). */
fun jalaliToEpoch(jy: Int, jm: Int, jd: Int): Long {
    var e = 19802L + Math.round((jy - 1403) * 365.2422) + (jm - 1) * 30L + (jd - 1)
    var guard = 0
    while (cmp(jalali(e), jy, jm, jd) > 0 && guard++ < 800) e--
    while (cmp(jalali(e), jy, jm, jd) < 0 && guard++ < 800) e++
    return e
}

fun startOfMonth(jy: Int, jm: Int): Long = jalaliToEpoch(jy, jm, 1)

fun jalaliMonthLength(jy: Int, jm: Int): Int {
    val next = if (jm == 12) startOfMonth(jy + 1, 1) else startOfMonth(jy, jm + 1)
    return (next - startOfMonth(jy, jm)).toInt()
}

/** The same day-of-month [k] months later; a day that does not exist (31 in a 30-day month, 30 Esfand) moves to the month's last day. */
fun addJalaliMonths(epochDay: Long, k: Int): Long {
    val j = jalali(epochDay)
    val total = j[0] * 12 + (j[1] - 1) + k
    val ny = Math.floorDiv(total, 12)
    val nm = Math.floorMod(total, 12) + 1
    return startOfMonth(ny, nm) + minOf(j[2], jalaliMonthLength(ny, nm)) - 1
}

fun jalaliFull(epochDay: Long): String {
    val j = jalali(epochDay)
    return "${j[2].fa()} ${JALALI_MONTHS[j[1] - 1]} ${j[0].fa()}"
}
