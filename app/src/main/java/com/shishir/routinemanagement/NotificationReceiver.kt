package com.shishir.routinemanagement

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val courseName = intent.getStringExtra("courseName") ?: "Class Reminder"

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "class_channel",
                "Class Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "class_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Upcoming Class!")
            .setContentText("You have $courseName soon.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
