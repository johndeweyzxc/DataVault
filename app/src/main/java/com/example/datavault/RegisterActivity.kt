package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val firebaseInstance = FirebaseAuth.getInstance()
        val contentActivity = Intent(this, ContentActivity::class.java)

        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User just created a new account
                startActivity(contentActivity)
            }
        }

        val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etRegisterPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etRegisterConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val btnRegisterAccount = findViewById<AppCompatButton>(R.id.btnRegisterAccount)
        val tvRegisterLoginInstead = findViewById<TextView>(R.id.tvRegisterLoginInstead)

        btnRegisterAccount.setOnClickListener {
            SignInWithEmail().signUp(
                this, etRegisterEmail, etRegisterPassword, etRegisterConfirmPassword)
        }

        tvRegisterLoginInstead.setOnClickListener {
            finish()
        }
    }
}