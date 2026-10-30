package ir.ehsananish.dstest

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.util.TimeZone

/** One optional evening reminder; one inexact alarm for the next time, re-planned after boot and time changes. */
object Reminders {
    const val ACTION_FIRE = "ir.ehsananish.dstest.FIRE"
    private const val CHANNEL = "evening_checkin"
    private const val NOTIFY_ID = 21

    private val TEXTS = listOf(
        "امروز چطور گذشت؟ ده ثانیه برای ثبت حالت وقت بگذار.",
        "ابری منتظر توست؛ حال امروزت را بنویس.",
        "یک شکرگزاری کوچک برای امروز؟ حال‌نامه را باز کن.",
        "قبل از خواب، حال امروز را ثبت کن تا فردا یادت بماند.",
    )

    fun needsPermission(ctx: Context): Boolean =
        Build.VERSION.SDK_INT >= 33 &&
            ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    fun canNotify(ctx: Context): Boolean =
        !needsPermission(ctx) && ctx.getSystemService(NotificationManager::class.java).areNotificationsEnabled()

    private fun channel(ctx: Context): String {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "یادآور شبانهٔ ثبت حال", NotificationManager.IMPORTANCE_DEFAULT))
        }
        return CHANNEL
    }

    private fun alarmIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, 1, Intent(ctx, ReminderReceiver::class.java).setAction(ACTION_FIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun reschedule(ctx: Context, store: Store) {
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val pi = alarmIntent(ctx)
        am.cancel(pi)
        if (!store.reminderOn) return
        val now = System.currentTimeMillis()
        val at = Logic.nextReminder(now, TimeZone.getDefault().getOffset(now).toLong(), store.reminderMin)
        runCatching { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) }
    }

    fun fire(ctx: Context, store: Store) {
        if (store.reminderOn && store.entryOn(store.today()) == null && canNotify(ctx)) {
            val open = Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("open_entry", true)
            val pi = PendingIntent.getActivity(ctx, 2, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val text = TEXTS[(System.currentTimeMillis() / 86_400_000L % TEXTS.size).toInt()]
            val n = Notification.Builder(ctx, channel(ctx))
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("حال‌نامه")
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .build()
            ctx.getSystemService(NotificationManager::class.java).notify(NOTIFY_ID, n)
        }
        reschedule(ctx, store)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = Store.get(context)
        if (intent.action == Reminders.ACTION_FIRE) Reminders.fire(context, store) else Reminders.reschedule(context, store)
    }
}
