package com.shishir.routinemanagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Toast.makeText(context, "Rescheduling class notifications...", Toast.LENGTH_SHORT).show()
            fetchClassesAndReschedule(context)
        }
    }

    private fun fetchClassesAndReschedule(context: Context) {
        val db = FirebaseFirestore.getInstance()
        db.collection("classes")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val courseName = document.getString("courseName") ?: continue
                    val timeString = document.getString("time") ?: continue
                    val day = document.getString("day") ?: continue

                    val classTimeMillis = calculateNextClassTime(day, timeString)

                    if (classTimeMillis > System.currentTimeMillis()) {
                        NotificationScheduler.scheduleNotification(context, courseName, classTimeMillis)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch classes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateNextClassTime(day: String, timeString: String): Long {
        val calendar = Calendar.getInstance()

        // Set calendar to today first
        calendar.timeInMillis = System.currentTimeMillis()

        // Find day difference
        val daysOfWeek = mapOf(
            "Sunday" to Calendar.SUNDAY,
            "Monday" to Calendar.MONDAY,
            "Tuesday" to Calendar.TUESDAY,
            "Wednesday" to Calendar.WEDNESDAY,
            "Thursday" to Calendar.THURSDAY,
            "Friday" to Calendar.FRIDAY,
            "Saturday" to Calendar.SATURDAY
        )

        val classDay = daysOfWeek[day] ?: Calendar.MONDAY

        val today = calendar.get(Calendar.DAY_OF_WEEK)

        var daysUntilClass = classDay - today
        if (daysUntilClass < 0) {
            daysUntilClass += 7 // Next week
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysUntilClass)

        // Parse class time (example: 01:45 AM)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(timeString)

        if (date != null) {
            val classTime = Calendar.getInstance()
            classTime.time = date
            calendar.set(Calendar.HOUR_OF_DAY, classTime.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, classTime.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        // Schedule notification 15 minutes before class
        calendar.add(Calendar.MINUTE, -15)

        return calendar.timeInMillis
    }
}
