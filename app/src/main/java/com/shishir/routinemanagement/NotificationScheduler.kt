import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shishir.routinemanagement.NotificationReceiver

object NotificationScheduler {

    fun scheduleNotification(context: Context, courseName: String, triggerAtMillis: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("courseName", courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            courseName.hashCode(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
