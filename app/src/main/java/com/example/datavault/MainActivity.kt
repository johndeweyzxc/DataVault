package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val firebaseInstance: FirebaseAuth = FirebaseAuth.getInstance()
        val contentActivity = Intent(this, ContentActivity::class.java)
        val registerActivity = Intent(this, RegisterActivity::class.java)

        val signUp = findViewById<TextView>(R.id.signUp)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)

        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User is signed in
                startActivity(contentActivity)
            }
        }

        loginBtn.setOnClickListener {
            Auth(firebaseInstance).signIn(this, emailInput, passwordInput)
        }

        signUp.setOnClickListener {
            startActivity(registerActivity)
        }
    }
}