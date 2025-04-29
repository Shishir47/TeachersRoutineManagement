package com.shishir.routinemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityEditClassBinding
import java.util.Calendar

class EditClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditClassBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var documentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        val intent = intent
        documentId = intent.getStringExtra("documentId") ?: ""

        if (documentId.isNotEmpty()) {
            binding.etCourseName.setText(intent.getStringExtra("courseName"))
            binding.etCourseCode.setText(intent.getStringExtra("courseCode"))
            binding.etSemester.setText(intent.getStringExtra("semester"))
            binding.etSection.setText(intent.getStringExtra("section"))
            binding.etRoomNumber.setText(intent.getStringExtra("roomNumber"))
            binding.etTime.setText(intent.getStringExtra("time"))

            val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            val adapter = ArrayAdapter(this, R.layout.spinner_item, days)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDay.adapter = adapter

            val currentDay = intent.getStringExtra("day")
            currentDay?.let {
                val spinnerPosition = days.indexOf(it)
                if (spinnerPosition >= 0) {
                    binding.spinnerDay.setSelection(spinnerPosition)
                }
            }

        } else {
            Toast.makeText(this, "Invalid class ID.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnSaveClass.setOnClickListener {
            saveClassDetails()
        }

        binding.etTime.setOnClickListener {
            openTimePicker()
        }
        binding.cancel.setOnClickListener {
            goToPrevious()
        }
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Select Class Time")
            .build()

        timePicker.show(supportFragmentManager, "TIME_PICKER")

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val hourForDisplay = when {
                selectedHour == 0 -> 12
                selectedHour > 12 -> selectedHour - 12
                else -> selectedHour
            }

            val amPm = if (selectedHour >= 12) "PM" else "AM"

            val formattedTime = String.format("%02d:%02d %s", hourForDisplay, selectedMinute, amPm)

            binding.etTime.setText(formattedTime)
        }
    }

    private fun goToPrevious() {
        val intent = Intent(this, ViewScheduleActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveClassDetails() {
        val courseName = binding.etCourseName.text.toString()
        val courseCode = binding.etCourseCode.text.toString()
        val semester = binding.etSemester.text.toString()
        val section = binding.etSection.text.toString()
        val roomNumber = binding.etRoomNumber.text.toString()
        val day = binding.spinnerDay.selectedItem.toString()
        val time = binding.etTime.text.toString()
        val teacherEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        if (courseName.isEmpty() || courseCode.isEmpty() || semester.isEmpty() ||
            section.isEmpty() || roomNumber.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val classDetails = hashMapOf(
            "courseName" to courseName,
            "courseCode" to courseCode,
            "semester" to semester,
            "section" to section,
            "roomNumber" to roomNumber,
            "day" to day,
            "time" to time,
            "teacherEmail" to teacherEmail
        )

        db.collection("classes").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("classes").document(documentId)
                        .set(classDetails)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show()
                            goToPrevious()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error updating class.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Class not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error accessing the class details.", Toast.LENGTH_SHORT).show()
            }
    }

}
