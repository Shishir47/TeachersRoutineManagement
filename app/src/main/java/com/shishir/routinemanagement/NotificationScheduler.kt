package com.shishir.routinemanagement

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object NotificationScheduler {

    fun scheduleNotification(context: Context, courseName: String, triggerAtMillis: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("courseName", courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            courseName.hashCode(), // Unique request code per course
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
