package com.example.datavault

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
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

        val signOutBtn = findViewById<Button>(R.id.signOut)
        signOutBtn.setOnClickListener {
            Auth(firebaseInstance).signOutUser()
        }
    }
}