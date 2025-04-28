package com.shishir.routinemanagement

import android.os.Bundle
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
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        loadTodayClasses()
    }

    private fun loadTodayClasses() {
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        db.collection("classes")
            .whereEqualTo("day", dayOfWeek)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val tableRow = TableRow(this)
                    tableRow.setPadding(0, 16, 0, 16)
                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )

                    val courseName = TextView(this)
                    courseName.text = document.getString("courseName")
                    courseName.setPadding(16, 16, 16, 16)
                    courseName.textSize = 18f
                    courseName.setTextColor(resources.getColor(android.R.color.black))
                    courseName.setTypeface(null, android.graphics.Typeface.BOLD)

                    val courseCode = TextView(this)
                    courseCode.text = document.getString("courseCode")
                    courseCode.setPadding(16, 16, 16, 16)
                    courseCode.textSize = 16f
                    courseCode.setTextColor(resources.getColor(android.R.color.darker_gray))

                    val time = TextView(this)
                    time.text = document.getString("time")
                    time.setPadding(16, 16, 16, 16)
                    time.textSize = 16f
                    time.setTextColor(resources.getColor(android.R.color.darker_gray))

                    val roomNumber = TextView(this)
                    roomNumber.text = document.getString("roomNumber")
                    roomNumber.setPadding(16, 16, 16, 16)
                    roomNumber.textSize = 16f
                    roomNumber.setTextColor(resources.getColor(android.R.color.darker_gray))

                    tableRow.addView(courseName)
                    tableRow.addView(courseCode)
                    tableRow.addView(time)
                    tableRow.addView(roomNumber)
                    binding.tableLayoutToday.addView(tableRow)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Fetch from Database", Toast.LENGTH_SHORT).show()
            }
    }

}
