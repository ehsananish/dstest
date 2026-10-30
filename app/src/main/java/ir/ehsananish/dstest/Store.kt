package ir.ehsananish.dstest

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

/** Diary entries, the PIN and reminder preferences; entries are written after every change. */
class Store private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("halnameh", Context.MODE_PRIVATE)

    var entries by mutableStateOf(Codec.fromText(prefs.getString("data", null)) ?: emptyList())
        private set

    var pinHash by mutableStateOf(prefs.getString("pin", null))
        private set
    var reminderOn by mutableStateOf(prefs.getBoolean("rem_on", false))
        private set
    var reminderMin by mutableStateOf(prefs.getInt("rem_min", 21 * 60 + 30))
        private set

    val hasPin get() = pinHash != null

    fun today(): Long = LocalDate.now().toEpochDay()

    private fun persist() { prefs.edit().putString("data", Codec.toText(entries)).apply() }

    fun entryOn(day: Long): Entry? = entries.firstOrNull { it.day == day }

    /** One entry per day: a new one replaces the old. Returns the replaced entry, if any. Future days are refused. */
    fun save(e: Entry): Entry? {
        if (!Logic.validMood(e.mood) || e.day > today()) return null
        val clean = e.copy(note = e.note.trim().take(Logic.MAX_NOTE), grat = e.grat.map { it.trim().take(Logic.MAX_GRAT) }.filter { it.isNotEmpty() }.take(3))
        val old = entryOn(e.day)
        entries = (entries.filter { it.day != e.day } + clean).sortedBy { it.day }
        persist()
        return old
    }

    fun remove(day: Long): Entry? {
        val old = entryOn(day) ?: return null
        entries = entries.filter { it.day != day }
        persist()
        return old
    }

    fun backupText(): String = Codec.toText(entries)

    /** False (nothing changed) if the text is not a حال‌نامه backup. */
    fun restoreBackup(text: String): Boolean {
        val d = Codec.fromText(text) ?: return false
        entries = d.filter { it.day <= today() + 1 }
        persist()
        return true
    }

    fun setPin(pin: String?) {
        pinHash = pin?.let { Logic.pinHash(it) }
        prefs.edit().apply { if (pinHash == null) remove("pin") else putString("pin", pinHash) }.apply()
    }

    fun checkPin(pin: String) = pinHash != null && Logic.pinHash(pin) == pinHash

    fun setReminder(on: Boolean, minute: Int = reminderMin) {
        reminderOn = on
        reminderMin = minute.coerceIn(0, 1439)
        prefs.edit().putBoolean("rem_on", reminderOn).putInt("rem_min", reminderMin).apply()
    }

    /** CI screenshots only: sixty believable days. */
    fun seedDemo() {
        if (entries.isNotEmpty()) return
        val t = today()
        val moods = intArrayOf(4, 3, 5, 4, 2, 3, 4, 5, 4, 3, 1, 2, 3, 4, 4, 5, 3, 4, 2, 4)
        val notes = listOf("پیاده‌روی عصر و چای با مامان", "روز شلوغ ولی تمام شد", "امتحان خوب بود", "", "سردرد و خستگی", "فیلم دیدیم", "")
        val list = ArrayList<Entry>()
        for (i in 59 downTo 0) {
            if (i % 9 == 4) continue
            val m = moods[(i * 7) % moods.size]
            val tags = HashSet<Int>()
            if (m >= 4) tags.add(if (i % 2 == 0) 4 else 0)
            if (m >= 4 && i % 3 == 0) tags.add(5)
            if (m <= 2) tags.add(if (i % 2 == 0) 14 else 6)
            if (i % 5 == 0) tags.add(1)
            list.add(Entry(t - i, m, tags, notes[i % notes.size], if (i % 4 == 0) listOf("چای داغ", "نور آفتاب") else emptyList()))
        }
        entries = list
        persist()
    }

    companion object {
        @Volatile private var instance: Store? = null

        fun get(context: Context): Store =
            instance ?: synchronized(this) { instance ?: Store(context.applicationContext).also { instance = it } }
    }
}
