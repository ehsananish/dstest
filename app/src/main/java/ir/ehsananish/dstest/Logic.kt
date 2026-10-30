package ir.ehsananish.dstest

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** One day in the diary. [mood] is 1..5, [tags] are ids from [TAGS]. */
data class Entry(
    val day: Long,
    val mood: Int,
    val tags: Set<Int> = emptySet(),
    val note: String = "",
    val grat: List<String> = emptyList(),
)

val MOOD_NAMES = listOf("بد", "ناخوش", "معمولی", "خوب", "عالی")

val TAGS = listOf(
    "خانواده", "دوستان", "کار", "درس", "ورزش", "خواب خوب", "کم‌خوابی", "طبیعت",
    "مطالعه", "سفر", "غذای خوب", "آرامش", "تفریح", "تنهایی", "استرس", "خرید",
)

val PROMPTS = listOf(
    "امروز چه چیز کوچکی تو را به لبخند آورد؟",
    "از کدام آدم زندگی‌ات امروز ممنونی؟",
    "چه چیزی امروز بهتر از انتظارت پیش رفت؟",
    "اگر امروز یک کلمه داشت، آن کلمه چه بود؟",
    "امروز چه کاری برای خودت کردی؟",
    "چه چیزی در بدنت امروز سپاس‌گزارش هستی؟",
    "کدام لحظهٔ امروز را دوست داری دوباره زندگی کنی؟",
    "چه چیزی امروز نگرانت کرد و حالا چقدر مهم است؟",
    "امروز چه چیز تازه‌ای یاد گرفتی؟",
    "یک غذا، بو یا صدای خوب امروز را بنویس.",
    "چه کسی را امروز خوشحال کردی؟",
    "اگر به خودت دیروز نامه می‌نوشتی، چه می‌گفتی؟",
    "چه چیزی را می‌توانی رها کنی تا سبک‌تر شوی؟",
    "امروز کجا آرام بودی؟",
    "یک کار کوچک که فردا انجام می‌دهی چیست؟",
    "چه چیز ساده‌ای در خانه‌ات دوستش داری؟",
    "کدام کار امروز را با تمام وجود انجام دادی؟",
    "امروز چه چیزی تو را خندانده؟",
    "از چه چیزی در زندگی‌ات حس غرور می‌کنی؟",
    "امروز چه مرزی برای خودت گذاشتی؟",
    "چه آهنگ یا جمله‌ای امروز با تو ماند؟",
    "اگر انرژی‌ات یک رنگ داشت، امروز چه رنگی بود؟",
    "چه چیز سختی را امروز تاب آوردی؟",
    "کدام عادت خوب را دوست داری ادامه بدهی؟",
    "امروز از چه کسی کمک گرفتی یا کمک کردی؟",
    "هوای امروز چه حسی به تو داد؟",
    "چه چیزی در آیندهٔ نزدیکت منتظرش هستی؟",
    "امروز چه چیزی را بخشیدی؟",
    "یک خاطرهٔ خوب از این هفته بنویس.",
    "چه چیزی کمتر از آنچه فکر می‌کردی سخت بود؟",
    "اگر می‌توانستی یک چیز امروز را عوض کنی، چه بود؟",
    "چه کسی دلت برایش تنگ شده؟ به او پیام بده.",
    "امروز چقدر آب خوردی و چقدر نفس عمیق کشیدی؟",
    "چه چیزی امروز تو را شگفت‌زده کرد؟",
    "چه ویژگی‌ای در خودت دوست داری؟",
    "کدام تصمیم امروز را پسندیدی؟",
    "چه کاری را به فردا موکول کردی و چرا؟",
    "یک چیز زیبا که دیدی را توصیف کن.",
    "امروز بدنت چه می‌خواست؟",
    "برای شب امشب چه آرزویی داری؟",
)

object Logic {
    const val MAX_NOTE = 1000
    const val MAX_GRAT = 120

    fun validMood(m: Int) = m in 1..5

    fun ascii(s: String): String = s.map {
        when (it) { in '۰'..'۹' -> '0' + (it - '۰'); in '٠'..'٩' -> '0' + (it - '٠'); else -> it }
    }.joinToString("")

    fun promptFor(day: Long): String = PROMPTS[Math.floorMod(day, PROMPTS.size.toLong()).toInt()]

    /** Saturday-first weekday offset (0 = Saturday … 6 = Friday). */
    fun satOffset(day: Long): Int = (weekdayIndex(day) + 1) % 7

    fun weekStart(day: Long): Long = day - satOffset(day)

    /** Consecutive days with an entry, ending today; if today is still empty the streak that ended yesterday still counts. */
    fun streak(days: Set<Long>, today: Long): Int {
        var d = if (today in days) today else today - 1
        var n = 0
        while (d in days) { n++; d-- }
        return n
    }

    fun longestStreak(days: Set<Long>): Int {
        var best = 0
        for (d in days) {
            if ((d - 1) in days) continue
            var n = 0
            var x = d
            while (x in days) { n++; x++ }
            if (n > best) best = n
        }
        return best
    }

    fun average(list: List<Entry>): Double? = if (list.isEmpty()) null else list.sumOf { it.mood }.toDouble() / list.size

    fun inRange(list: List<Entry>, from: Long, to: Long) = list.filter { it.day in from..to }

    /** One decimal with the Persian decimal mark: 3.84 → «۳٫۸». */
    fun fmt1(v: Double): String =
        String.format(java.util.Locale.US, "%.1f", v).replace('.', '٫').map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

    /** Plain-Persian conclusion comparing the last 7 days with the 7 before. */
    fun weekSentence(list: List<Entry>, today: Long): String {
        val cur = inRange(list, today - 6, today)
        val prev = inRange(list, today - 13, today - 7)
        if (cur.size < 2) return "با چند روز ثبت، خلاصهٔ هفتهٔ تو اینجا نوشته می‌شود."
        val a = average(cur)!!
        if (prev.size < 2) return "میانگین حال این هفتهٔ تو ${fmt1(a)} از ۵ بوده."
        val b = average(prev)!!
        val pct = Math.round((a - b) / b * 100).toInt()
        return when {
            pct >= 5 -> "این هفته حالت ${pct.toString().toFa()}٪ بهتر از هفتهٔ قبل بوده."
            pct <= -5 -> "این هفته حالت ${(-pct).toString().toFa()}٪ پایین‌تر از هفتهٔ قبل بوده؛ به خودت مهربان باش."
            else -> "حال این هفته‌ات تقریباً مثل هفتهٔ قبل بوده."
        }
    }

    fun String.toFa(): String = map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")

    /** Average mood per weekday, Saturday first; null where there is no entry. */
    fun weekdayAverages(list: List<Entry>): List<Double?> {
        val sum = DoubleArray(7); val n = IntArray(7)
        list.forEach { val i = satOffset(it.day); sum[i] += it.mood.toDouble(); n[i]++ }
        return List(7) { if (n[it] == 0) null else sum[it] / n[it] }
    }

    data class TagImpact(val tag: Int, val withAvg: Double, val withoutAvg: Double, val n: Int) {
        val diff get() = withAvg - withoutAvg
    }

    /** Mood with a tag minus mood without it; needs at least [minSamples] days on both sides. Best first. */
    fun tagImpacts(list: List<Entry>, minSamples: Int = 3): List<TagImpact> {
        val out = ArrayList<TagImpact>()
        for (t in TAGS.indices) {
            val with = list.filter { t in it.tags }
            val without = list.filter { t !in it.tags }
            if (with.size < minSamples || without.size < minSamples) continue
            out.add(TagImpact(t, average(with)!!, average(without)!!, with.size))
        }
        return out.sortedByDescending { it.diff }
    }

    /** Entries matching [query] (note, gratitude, tag names; any digit style), optionally one mood and/or one tag. */
    fun search(list: List<Entry>, query: String, mood: Int = 0, tag: Int = -1): List<Entry> {
        val q = ascii(query).trim().lowercase()
        return list.filter { e ->
            (mood == 0 || e.mood == mood) && (tag < 0 || tag in e.tags) &&
                (q.isEmpty() || ascii(e.note).lowercase().contains(q) || e.grat.any { ascii(it).lowercase().contains(q) } ||
                    e.tags.any { TAGS.getOrNull(it)?.contains(q) == true })
        }
    }

    fun pinHash(pin: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(("halnameh|" + ascii(pin)).toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }

    /** UTC millis of the next [minuteOfDay] strictly after [nowMs] in a zone with offset [offsetMs]. */
    fun nextReminder(nowMs: Long, offsetMs: Long, minuteOfDay: Int): Long {
        val local = nowMs + offsetMs
        val dayStart = Math.floorDiv(local, 86_400_000L) * 86_400_000L
        var t = dayStart + minuteOfDay * 60_000L
        if (t <= local) t += 86_400_000L
        return t - offsetMs
    }

    /** «۲۱:۳۰» from minutes of the day. */
    fun hhmm(min: Int): String = "%02d:%02d".format(min / 60, min % 60).toFa()
}

object Codec {
    private const val MAGIC = "halnameh-backup"

    fun toText(list: List<Entry>): String {
        val o = JSONObject().put("magic", MAGIC).put("v", 1)
        o.put("e", JSONArray().also { a ->
            list.forEach { e ->
                a.put(JSONObject().put("d", e.day).put("m", e.mood).put("t", JSONArray(e.tags.sorted())).put("n", e.note).put("g", JSONArray(e.grat)))
            }
        })
        return o.toString()
    }

    /** Null if the text is not a backup of this app; damaged single records are skipped. */
    fun fromText(text: String?): List<Entry>? {
        if (text.isNullOrBlank()) return null
        val o = runCatching { JSONObject(text.trim()) }.getOrNull() ?: return null
        if (o.optString("magic") != MAGIC) return null
        val out = ArrayList<Entry>()
        o.optJSONArray("e")?.let { a ->
            for (i in 0 until a.length()) runCatching {
                val x = a.getJSONObject(i)
                val m = x.getInt("m")
                if (!Logic.validMood(m)) return@runCatching false
                val tags = HashSet<Int>()
                x.optJSONArray("t")?.let { ta -> for (k in 0 until ta.length()) ta.optInt(k, -1).takeIf { it in TAGS.indices }?.let(tags::add) }
                val g = ArrayList<String>()
                x.optJSONArray("g")?.let { ga -> for (k in 0 until minOf(3, ga.length())) ga.optString(k).trim().takeIf { it.isNotEmpty() }?.let { g.add(it.take(Logic.MAX_GRAT)) } }
                out.add(Entry(x.getLong("d"), m, tags, x.optString("n").take(Logic.MAX_NOTE), g))
            }
        }
        return out.distinctBy { it.day }.sortedBy { it.day }
    }
}
