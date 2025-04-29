package com.shishir.routinemanagement

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityTeacherDashboardBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Calendar

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDashboardBinding
    companion object {
        private const val REQUEST_WRITE_STORAGE = 100
    }
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddClass.setOnClickListener {
            startActivity(Intent(this, AddClassActivity::class.java))
        }

        binding.btnViewSchedule.setOnClickListener {
            startActivity(Intent(this, ViewScheduleActivity::class.java))
        }

        binding.btnTodayClass.setOnClickListener {
            startActivity(Intent(this, TodayClassActivity::class.java))
        }
        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        fetchClassesAndScheduleNotifications()
        checkForUpdates()
    }

    private fun fetchClassesAndScheduleNotifications() {
        val db = FirebaseFirestore.getInstance()
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return

        // Fetch classes filtered by the teacher's email
        db.collection("classes")
            .whereEqualTo("teacherEmail", currentUserEmail)  // Filter by teacher's email
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val courseName = document.getString("courseName") ?: continue
                    val day = document.getString("day") ?: continue
                    val time = document.getString("time") ?: continue

                    val triggerTimeMillis = calculateNextTriggerTime(day, time)
                    if (triggerTimeMillis != null) {
                        scheduleNotification(courseName, triggerTimeMillis)
                    }
                }
            }
    }

    fun scheduleNotification(courseName: String, classTimeMillis: Long) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("courseName", courseName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (courseName + classTimeMillis).hashCode(), // Unique ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        // Repeat every 7 days (1 week)
        val intervalWeekMillis = 7 * 24 * 60 * 60 * 1000L

        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            classTimeMillis - (15 * 60 * 1000), // 15 mins before class
            intervalWeekMillis,
            pendingIntent
        )
    }

    private fun calculateNextTriggerTime(day: String, time: String): Long? {
        val calendar = Calendar.getInstance()

        val daysOfWeek = mapOf(
            "Sunday" to Calendar.SUNDAY,
            "Monday" to Calendar.MONDAY,
            "Tuesday" to Calendar.TUESDAY,
            "Wednesday" to Calendar.WEDNESDAY,
            "Thursday" to Calendar.THURSDAY,
            "Friday" to Calendar.FRIDAY,
            "Saturday" to Calendar.SATURDAY
        )

        val targetDay = daysOfWeek[day] ?: return null

        // Parse time
        val timeParts = time.split(":", " ")
        var hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        val amPm = timeParts[2]

        if (amPm == "PM" && hour != 12) {
            hour += 12
        } else if (amPm == "AM" && hour == 12) {
            hour = 0
        }

        // Set target day and time
        calendar.set(Calendar.DAY_OF_WEEK, targetDay)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If the scheduled time is before now, set for next week
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }


    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout Confirmation")
        builder.setMessage("Are you sure you want to log out?")

        builder.setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForUpdates()
            } else {
                Toast.makeText(this, "Permission required to download updates", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun checkForUpdates() {
        val url =
            "https://raw.githubusercontent.com/Shishir47/TeachersRoutineManagement/master/version.json"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(     object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@TeacherDashboardActivity,
                                        "Failed to check for updates",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = JSONObject(responseBody.string())
                        val latestVersionCode = json.getInt("versionCode")
                        val apkUrl = json.getString("apkUrl")
                        val currentVersionCode =
                            packageManager.getPackageInfo(packageName, 0).versionCode

                        if (latestVersionCode > currentVersionCode) {
                            runOnUiThread {
                                showUpdateDialog(apkUrl)
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Failed to fetch update info",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun showUpdateDialog(apkUrl: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Update Available")
        builder.setMessage("A newer version of the app is available. Please update to the latest version.")
        builder.setPositiveButton("Update") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, apkUrl.toUri())
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(false)
        builder.show()
    }

}
