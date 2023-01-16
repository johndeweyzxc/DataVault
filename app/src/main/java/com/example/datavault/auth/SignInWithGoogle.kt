package com.example.datavault.auth

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class FirebaseAuthWithGoogle(private val idToken: String, private val mainContext: Context) {

    fun signIn() {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    mainContext,
                    "Successfully signed in with google",
                    Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(
                    mainContext,
                    "Failed signing in with google",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}