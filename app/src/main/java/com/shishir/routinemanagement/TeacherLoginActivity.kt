package com.shishir.routinemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.shishir.routinemanagement.databinding.ActivityTeacherLoginBinding

class TeacherLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            val intent= Intent(this, TeacherDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        startActivity(Intent(this, TeacherDashboardActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, TeacherRegisterActivity::class.java))
        }
    }
}
