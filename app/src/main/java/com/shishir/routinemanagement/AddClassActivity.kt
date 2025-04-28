package com.shishir.routinemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityAddClassBinding
import java.util.Calendar

class AddClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddClassBinding
    private val db = FirebaseFirestore.getInstance()
    private var selectedHour = 0
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDay.adapter = adapter

        binding.etTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.btnAddClass.setOnClickListener {
            addClass()
        }
    }

    private fun showTimePickerDialog() {
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
            binding.tvSelectedTime.text = formattedTime
            binding.cancel.setOnClickListener {
                goToPrevious()
            }
        }
    }


    private fun goToPrevious(){
        val intent= Intent(this, ViewScheduleActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun addClass() {
        val courseName = binding.etCourseName.text.toString()
        val courseCode = binding.etCourseCode.text.toString()
        val semester = binding.etSemester.text.toString()
        val section = binding.etSection.text.toString()
        val roomNumber = binding.etRoomNumber.text.toString()
        val time = binding.tvSelectedTime.text.toString()
        val day = binding.spinnerDay.selectedItem.toString()

        if (courseName.isEmpty() || courseCode.isEmpty() || semester.isEmpty() || section.isEmpty() || roomNumber.isEmpty() || time == "No time selected") {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("classes")
            .whereEqualTo("day", day)
            .whereEqualTo("time", time)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Class already exists at this time.", Toast.LENGTH_SHORT).show()
                } else {
                    val classDetails = hashMapOf(
                        "courseName" to courseName,
                        "courseCode" to courseCode,
                        "semester" to semester,
                        "section" to section,
                        "roomNumber" to roomNumber,
                        "day" to day,
                        "time" to time
                    )

                    db.collection("classes")
                        .add(classDetails)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Class added successfully!", Toast.LENGTH_SHORT).show()
                            goToPrevious()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error adding class.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
}
