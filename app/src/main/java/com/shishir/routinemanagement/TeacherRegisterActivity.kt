package com.shishir.routinemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shishir.routinemanagement.databinding.ActivityTeacherRegisterBinding

class TeacherRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val user = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "phone" to phone
                        )
                        db.collection("teachers").document(auth.currentUser!!.uid)
                            .set(user)
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, TeacherDashboardActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, TeacherLoginActivity::class.java))
        }
    }
}
