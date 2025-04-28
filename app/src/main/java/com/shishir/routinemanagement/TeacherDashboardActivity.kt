package com.shishir.routinemanagement

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.shishir.routinemanagement.databinding.ActivityTeacherDashboardBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

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
            finish()
        }

        binding.btnViewSchedule.setOnClickListener {
            startActivity(Intent(this, ViewScheduleActivity::class.java))
            finish()
        }

        binding.btnTodayClass.setOnClickListener {
            startActivity(Intent(this, TodayClassActivity::class.java))
            finish()
        }
        binding.logout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

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
            "https://raw.githubusercontent.com/Shishir47/Blood_Donation_App/master/version.json"

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
