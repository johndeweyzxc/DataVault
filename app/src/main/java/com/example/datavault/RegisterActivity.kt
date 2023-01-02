package com.example.datavault


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

        val signUp = findViewById<TextView>(R.id.loginInstead)
        val email = findViewById<EditText>(R.id.createEmail)
        val password = findViewById<EditText>(R.id.createPassword)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val createAccount = findViewById<Button>(R.id.createAccount)

        createAccount.setOnClickListener {
            Auth(firebaseInstance).signUp(this, email, password, confirmPassword)
        }

        signUp.setOnClickListener {
            finish()
        }
    }
}