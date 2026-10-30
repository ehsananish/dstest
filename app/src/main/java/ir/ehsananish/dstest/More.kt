package ir.ehsananish.dstest

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.ehsananish.dstest.ds.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Locked until the right PIN is typed; survives rotation, resets when the app goes to the background. */
object AppLock {
    var unlocked by mutableStateOf(false)
}

@Composable
fun MoreScreen(store: Store) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var pinDialog by rememberSaveable { mutableStateOf(false) }
    var pendingRestore by rememberSaveable { mutableStateOf<String?>(null) }
    var askedPermission by rememberSaveable { mutableStateOf(false) }

    TabBody { say ->
        val scope = rememberCoroutineScope()
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) { store.setReminder(true); Reminders.reschedule(ctx, store); say.say("یادآور روشن شد.") }
            else say.say("برای یادآور باید اجازهٔ اعلان را بدهی.")
        }
        val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val text = store.backupText()
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching { ctx.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray()) } != null }.getOrDefault(false)
                }
                say.say(if (ok) "پشتیبان در فایل ذخیره شد." else "ذخیرهٔ فایل انجام نشد.")
            }
        }
        val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching { ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
                }
                if (text != null && Codec.fromText(text) != null) pendingRestore = text
                else say.say("این فایل پشتیبان حال‌نامه نیست؛ چیزی تغییر نکرد.")
            }
        }

        ScreenHeader("بیشتر", subtitle = "یادآور، رمز و پشتیبان")
        Column(Modifier.padding(horizontal = Space.md), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            PastelCard(Modifier.fillMaxWidth(), color = cs.tertiaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    CloudMascot(MascotMood.Happy, size = 96.dp)
                    Column(Modifier.weight(1f)) {
                        Text("همه‌چیز فقط روی گوشی توست", style = MaterialTheme.typography.titleMedium)
                        Text("بدون حساب، بدون اینترنت و بدون تبلیغ. رمز بگذار تا دفترت خصوصی بماند.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            SettingsGroup(title = "یادآور شبانه") {
                SettingsGroupRow(
                    Icons.Rounded.Notifications, "یادآور ثبت حال",
                    subtitle = if (store.reminderOn) "هر شب ${Logic.hhmm(store.reminderMin)}، اگر آن روز ثبت نکرده باشی" else "خاموش",
                    pair = 3,
                    onClick = null,
                    trailing = {
                        Switch(store.reminderOn, { on ->
                            if (!on) { store.setReminder(false); Reminders.reschedule(ctx, store) }
                            else if (Reminders.needsPermission(ctx) && !askedPermission) { askedPermission = true; permission.launch(Manifest.permission.POST_NOTIFICATIONS) }
                            else { store.setReminder(true); Reminders.reschedule(ctx, store); if (!Reminders.canNotify(ctx)) say.say("اعلان‌ها برای حال‌نامه در تنظیمات گوشی خاموش است.") }
                        })
                    },
                )
                SettingsGroupRow(
                    Icons.Rounded.Schedule, "ساعت یادآور", value = Logic.hhmm(store.reminderMin), pair = 1, divider = false,
                    onClick = {
                        TimePickerDialog(ctx, { _, h, m -> store.setReminder(store.reminderOn, h * 60 + m); Reminders.reschedule(ctx, store) }, store.reminderMin / 60, store.reminderMin % 60, true).show()
                    },
                )
            }

            SettingsGroup(title = "حریم خصوصی") {
                SettingsGroupRow(
                    Icons.Rounded.Lock, if (store.hasPin) "رمز ورود روشن است" else "گذاشتن رمز ۴ رقمی",
                    subtitle = if (store.hasPin) "با ضربه، رمز را بردار" else "هر بار که برنامه باز می‌شود رمز پرسیده می‌شود",
                    pair = 5, divider = false,
                    onClick = { if (store.hasPin) { store.setPin(null); say.say("رمز برداشته شد.") } else pinDialog = true },
                )
            }

            SettingsGroup(title = "پشتیبان و خروجی") {
                SettingsGroupRow(Icons.Rounded.Backup, "ذخیرهٔ پشتیبان در فایل", subtitle = "همهٔ روزها، برچسب‌ها و یادداشت‌ها", pair = 2, onClick = { saveFile.launch("halnameh-backup.json") })
                SettingsGroupRow(Icons.Rounded.Restore, "بازیابی از فایل پشتیبان", subtitle = "جایگزین دفتر فعلی می‌شود", pair = 6, onClick = { openFile.launch(arrayOf("application/json", "text/plain", "*/*")) })
                SettingsGroupRow(
                    Icons.Rounded.Share, "فرستادن دفتر به‌صورت متن", subtitle = "برای خودت یا یک نسخهٔ چاپی", pair = 4, divider = false,
                    onClick = {
                        if (store.entries.isEmpty()) say.say("هنوز چیزی برای فرستادن نیست.")
                        else ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, exportText(store.entries)), "اشتراک‌گذاری دفتر"))
                    },
                )
            }
            Spacer(Modifier.height(Space.sm))
        }
        if (pinDialog) PinSetDialog({ pinDialog = false }) { store.setPin(it); pinDialog = false; say.say("رمز گذاشته شد.") }
        pendingRestore?.let { text ->
            AlertDialog(
                onDismissRequest = { pendingRestore = null },
                title = { Text("بازیابی پشتیبان؟") },
                text = { Text("دفتر فعلی با محتوای فایل جایگزین می‌شود.") },
                confirmButton = { TextButton({ val ok = store.restoreBackup(text); pendingRestore = null; say.say(if (ok) "پشتیبان بازیابی شد." else "بازیابی انجام نشد.") }) { Text("جایگزین کن") } },
                dismissButton = { TextButton({ pendingRestore = null }) { Text("انصراف") } },
            )
        }
    }
}

fun exportText(list: List<Entry>): String {
    val sb = StringBuilder("دفتر حال‌نامه\n\n")
    list.reversed().forEach { e ->
        sb.append(jalaliLong(e.day)).append(" — حال ").append(MOOD_NAMES[e.mood - 1]).append('\n')
        if (e.tags.isNotEmpty()) sb.append("برچسب‌ها: ").append(e.tags.sorted().joinToString("، ") { TAGS[it] }).append('\n')
        if (e.note.isNotBlank()) sb.append(e.note).append('\n')
        e.grat.forEach { sb.append("ممنون: ").append(it).append('\n') }
        sb.append('\n')
    }
    return sb.toString().map { if (it in '0'..'9') '۰' + (it - '0') else it }.joinToString("")
}

@Composable
private fun PinSetDialog(onDismiss: () -> Unit, onSet: (String) -> Unit) {
    var a by rememberSaveable { mutableStateOf("") }
    var b by rememberSaveable { mutableStateOf("") }
    val ok = a.length == 4 && a == b
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رمز ۴ رقمی") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                Text("رمز را فراموش نکن؛ راه بازگرداندن آن نیست، فقط پاک کردن داده‌های برنامه.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(a, { a = Logic.ascii(it).filter(Char::isDigit).take(4) }, label = { Text("رمز") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                OutlinedTextField(b, { b = Logic.ascii(it).filter(Char::isDigit).take(4) }, label = { Text("تکرار رمز") }, singleLine = true,
                    isError = b.length == 4 && a != b, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
            }
        },
        confirmButton = { TextButton({ onSet(a) }, enabled = ok) { Text("ذخیره") } },
        dismissButton = { TextButton(onDismiss) { Text("انصراف") } },
    )
}

/** Full-screen PIN pad shown before anything else when a PIN is set. */
@Composable
fun LockScreen(store: Store) {
    val cs = MaterialTheme.colorScheme
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }
    fun press(d: String) {
        if (pin.length >= 4) return
        pin += d; wrong = false
        if (pin.length == 4) {
            if (store.checkPin(pin)) AppLock.unlocked = true else { wrong = true; pin = "" }
        }
    }
    Column(
        Modifier.fillMaxSize().background(cs.background).statusBarsPadding().navigationBarsPadding().padding(Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        CloudMascot(if (wrong) MascotMood.Sleepy else MascotMood.Happy, size = 130.dp)
        Text("حال‌نامه قفل است", style = MaterialTheme.typography.headlineSmall)
        Text(if (wrong) "رمز درست نبود، دوباره بزن" else "رمز ۴ رقمی را وارد کن", style = MaterialTheme.typography.bodyMedium, color = if (wrong) cs.error else cs.onSurfaceVariant)
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            for (i in 0 until 4) Box(Modifier.size(16.dp).clip(CircleShape).background(if (i < pin.length) cs.primary else cs.surfaceContainerHighest))
        }
        Spacer(Modifier.height(Space.lg))
        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫"))
        rows.forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md), modifier = Modifier.padding(vertical = 6.dp)) {
                r.forEach { k ->
                    if (k.isEmpty()) Spacer(Modifier.size(68.dp))
                    else Box(
                        Modifier.size(68.dp).clip(CircleShape).background(cs.surfaceContainerHigh).pressScale()
                            .clickable { if (k == "⌫") { pin = pin.dropLast(1) } else press(k) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (k == "⌫") Icon(Icons.AutoMirrored.Rounded.Backspace, "پاک کردن") else Text(k.fa(), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
