package com.shishir.routinemanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityViewScheduleBinding

class ViewScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewScheduleBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener {
            val intent= Intent(this, TeacherDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
        displaySchedule()

    }



    private fun displaySchedule() {
        db.collection("classes")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No classes found.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tableLayout.removeAllViews()

                    for (document in documents) {
                        val courseName = document.getString("courseName") ?: "Unknown Course"
                        val courseCode = document.getString("courseCode") ?: "Unknown Code"
                        val semester = document.getString("semester") ?: "Unknown Semester"
                        val section = document.getString("section") ?: "Unknown Section"
                        val roomNumber = document.getString("roomNumber") ?: "Unknown Room"
                        val day = document.getString("day") ?: "Unknown Day"
                        val time = document.getString("time") ?: "Unknown Time"
                        val documentId = document.id

                        val row = TableRow(this)
                        row.setPadding(8, 8, 8, 8)
                        row.layoutParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                        )

                        val courseTextLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.7f) // Increased width
                        val textLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f)
                        val dayTextLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.4f) // Slightly increased
                        val iconLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f)

                        val courseText = TextView(this).apply {
                            text = "$courseName\n($courseCode)"
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment= View.TEXT_ALIGNMENT_CENTER
                            layoutParams = courseTextLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        val semesterText = TextView(this).apply {
                            text = "$semester\n($section)"
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            textAlignment= View.TEXT_ALIGNMENT_CENTER
                            layoutParams = textLayoutParams
                            setTextColor(Color.BLACK)
                        }

                        val roomNumberText = TextView(this).apply {
                            text = roomNumber
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            layoutParams = textLayoutParams
                            textAlignment= View.TEXT_ALIGNMENT_CENTER
                            setTextColor(Color.BLACK)
                        }

                        val dayText = TextView(this).apply {
                            text = day
                            setPadding(7, 8, 7, 8)
                            textSize = 14f
                            layoutParams = courseTextLayoutParams
                            textAlignment= View.TEXT_ALIGNMENT_CENTER
                            setTextColor(Color.BLACK)
                        }

                        val timeText = TextView(this).apply {
                            text = time
                            setPadding(8, 8, 8, 8)
                            textSize = 14f
                            layoutParams = dayTextLayoutParams
                            textAlignment= View.TEXT_ALIGNMENT_CENTER
                            setTextColor(Color.BLACK)
                        }

                        val editIcon = ImageView(this).apply {
                            setImageResource(R.drawable.ic_edit)
                            background = null
                            setPadding(16, 16, 16, 16)
                            layoutParams = iconLayoutParams
                            setOnClickListener {
                                val intent = Intent(this@ViewScheduleActivity, EditClassActivity::class.java).apply {
                                    putExtra("documentId", documentId)
                                    putExtra("courseName", courseName)
                                    putExtra("courseCode", courseCode)
                                    putExtra("semester", semester)
                                    putExtra("section", section)
                                    putExtra("roomNumber", roomNumber)
                                    putExtra("day", day)
                                    putExtra("time", time)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }

                        val deleteIcon = ImageView(this).apply {
                            setImageResource(R.drawable.ic_delete)
                            background = null
                            setPadding(16, 16, 16, 16)
                            layoutParams = iconLayoutParams
                            setOnClickListener {
                                showDeleteConfirmationDialog(documentId)
                            }
                        }

                        row.addView(courseText)
                        row.addView(semesterText)
                        row.addView(roomNumberText)
                        row.addView(dayText)
                        row.addView(timeText)
                        row.addView(editIcon)
                        row.addView(deleteIcon)

                        binding.tableLayout.addView(row)
                    }

                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching classes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog(documentId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this class?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteClass(documentId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun deleteClass(documentId: String) {
        db.collection("classes").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Class deleted successfully!", Toast.LENGTH_SHORT).show()
                displaySchedule()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting class.", Toast.LENGTH_SHORT).show()
            }
    }
}
