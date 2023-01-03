package com.example.datavault

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
                finish()
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
        tvDisplayName.text = email

        val signOutBtn = findViewById<Button>(R.id.signOut)
        signOutBtn.setOnClickListener {
            SignInWithEmail().signOutUser()
            GoogleSignIn.getClient(this, gso).signOut()
        }
    }
}