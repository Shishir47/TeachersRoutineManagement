package com.shishir.routinemanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityTodayClassBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTodayClassBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, TeacherDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        displayTodayClasses()
    }

    private fun displayTodayClasses() {
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        db.collection("classes")
            .whereEqualTo("day", dayOfWeek)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No classes found for today.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tableLayoutToday.removeAllViews()

                    for (document in documents) {
                        val courseName = document.getString("courseName") ?: "Unknown Course"
                        val courseCode = document.getString("courseCode") ?: "Unknown Code"
                        val semester = document.getString("semester") ?: "Unknown Semester"
                        val section = document.getString("section") ?: "Unknown Section"
                        val roomNumber = document.getString("roomNumber") ?: "Unknown Room"
                        val time = document.getString("time") ?: "Unknown Time"

                        val row = TableRow(this)
                        row.setPadding(8, 8, 8, 8)
                        row.layoutParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                        )

                        val courseTextLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.7f)
                        val textLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f)
                        val timeTextLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.4f)

                        val courseText = TextView(this).apply {
                            text = "$courseName\n($courseCode)"
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = courseTextLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        val semesterText = TextView(this).apply {
                            text = "$semester\n($section)"
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = textLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        val roomNumberText = TextView(this).apply {
                            text = roomNumber
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = textLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        val timeText = TextView(this).apply {
                            text = time
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = timeTextLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        row.addView(courseText)
                        row.addView(semesterText)
                        row.addView(roomNumberText)
                        row.addView(timeText)

                        binding.tableLayoutToday.addView(row)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch today's classes.", Toast.LENGTH_SHORT).show()
            }
    }
}
