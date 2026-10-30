package ir.ehsananish.dstest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ir.ehsananish.dstest.ds.*

/** Counts up whenever a milestone should trigger the confetti overlay in MainActivity. */
object Party {
    var trigger by mutableIntStateOf(0)
}

private val MILESTONES = setOf(3, 7, 14, 30, 60, 100, 200, 365)

/** Message for a save that earned a celebration, or null. */
fun milestoneMessage(before: List<Entry>, after: List<Entry>, today: Long): String? {
    if (before.isEmpty() && after.isNotEmpty()) return "اولین ثبتت! ابری خوشحال شد."
    val s0 = Logic.streak(before.map { it.day }.toSet(), today)
    val s1 = Logic.streak(after.map { it.day }.toSet(), today)
    return if (s1 > s0 && s1 in MILESTONES) "${s1.fa()} روز پیاپی! آفرین." else null
}

/**
 * Bottom sheet to log or edit one day. [initialMood] pre-selects a face (0 = keep the saved one).
 * The arrows move to the neighbouring day without closing the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EntrySheet(store: Store, startDay: Long, initialMood: Int, onDismiss: () -> Unit) {
    val say = LocalSay.current
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = store.today()
    var day by rememberSaveable { mutableStateOf(startDay) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        key(day) {
            val old = store.entryOn(day)
            var mood by rememberSaveable { mutableIntStateOf(if (initialMood != 0 && day == startDay) initialMood else old?.mood ?: 0) }
            var tags by rememberSaveable { mutableStateOf((old?.tags ?: emptySet()).sorted()) }
            var note by rememberSaveable { mutableStateOf(old?.note ?: "") }
            var g1 by rememberSaveable { mutableStateOf(old?.grat?.getOrNull(0) ?: "") }
            var g2 by rememberSaveable { mutableStateOf(old?.grat?.getOrNull(1) ?: "") }
            var g3 by rememberSaveable { mutableStateOf(old?.grat?.getOrNull(2) ?: "") }

            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = Space.md).navigationBarsPadding().imePadding(),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, { day -= 1 }, contentDescription = "روز قبل")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(jalaliLong(day), style = MaterialTheme.typography.titleMedium)
                        Text(daysAgoText(today - day), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (day < today) CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, { day += 1 }, contentDescription = "روز بعد") else Spacer(Modifier.size(44.dp))
                }
                Text("حالت چطور بود؟", style = MaterialTheme.typography.titleSmall)
                MoodPicker(mood, { mood = it })
                Text("چه چیزی روی حالت اثر داشت؟", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    TAGS.forEachIndexed { i, name ->
                        FilterChip(
                            selected = i in tags,
                            onClick = { tags = if (i in tags) tags - i else (tags + i).sorted() },
                            label = { Text(name) },
                        )
                    }
                }
                ChatBubble(Logic.promptFor(day), title = "پرسش امروز")
                OutlinedTextField(
                    note, { note = it.take(Logic.MAX_NOTE) }, Modifier.fillMaxWidth(), minLines = 3, maxLines = 6,
                    label = { Text("یادداشت (اختیاری)") }, supportingText = { Text("${note.length.fa()} از ${Logic.MAX_NOTE.fa()}") },
                )
                Text("سه چیزی که ازشان ممنونی", style = MaterialTheme.typography.titleSmall)
                listOf(Triple("الف", g1, { v: String -> g1 = v }), Triple("ب", g2, { v: String -> g2 = v }), Triple("ج", g3, { v: String -> g3 = v })).forEachIndexed { i, (_, v, set) ->
                    OutlinedTextField(
                        v, { set(it.take(Logic.MAX_GRAT)) }, Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("شکرگزاری ${(i + 1).fa()}") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }
                PillButton(
                    if (old == null) "ذخیره" else "به‌روزرسانی", {
                        val before = store.entries
                        val replaced = store.save(Entry(day, mood, tags.toSet(), note, listOf(g1, g2, g3)))
                        val msg = milestoneMessage(before, store.entries, today)
                        if (msg != null) Party.trigger++
                        say?.say(msg ?: "ثبت شد: حال ${MOOD_NAMES[mood - 1]}، ${jalaliShort(day)}", if (replaced != null) "برگرداندن" else null) { replaced?.let { store.save(it) } }
                        onDismiss()
                    },
                    Modifier.fillMaxWidth(), icon = Icons.Rounded.Check, enabled = mood != 0,
                )
                if (old != null) {
                    PillButton("حذف این روز", {
                        val removed = store.remove(day)
                        say?.say("روز ${jalaliShort(day)} حذف شد.", "برگرداندن") { removed?.let { store.save(it) } }
                        onDismiss()
                    }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Delete, style = BtnStyle.Outlined)
                }
                Spacer(Modifier.height(Space.md))
            }
        }
    }
}
