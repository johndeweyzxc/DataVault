package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ContentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        val firebaseInstance = FirebaseAuth.getInstance()

        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                // User is not signed in
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val tvDisplayName = findViewById<TextView>(R.id.tvContentDisplayName)
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val provider = intent.getStringExtra("provider")
        tvDisplayName.text = email

        val signOutBtn = findViewById<Button>(R.id.signOut)
        signOutBtn.setOnClickListener {
            firebaseInstance.signOut()
            GoogleSignIn.getClient(this, gso).signOut()
        }
    }
}