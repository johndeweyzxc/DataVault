package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val firebaseInstance: FirebaseAuth = FirebaseAuth.getInstance()
        val contentActivity = Intent(this, ContentActivity::class.java)
        val registerActivity = Intent(this, RegisterActivity::class.java)

        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val tvLoginForgotPassword = findViewById<TextView>(R.id.tvLoginForgotPassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val tvLoginSignUp = findViewById<TextView>(R.id.tvLoginSignUp)
        val btnLoginWithGoogle = findViewById<AppCompatButton>(R.id.btnLoginWithGoogle)

        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User is signed in
                startActivity(contentActivity)
            }
        }

        btnLogin.setOnClickListener {
            Auth(firebaseInstance).signIn(this, etLoginEmail, etLoginPassword)
        }

        tvLoginSignUp.setOnClickListener {
            startActivity(registerActivity)
        }
    }
}