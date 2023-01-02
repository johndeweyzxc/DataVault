package com.example.datavault

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class Auth(private val firebaseInstanceAuth: FirebaseAuth) {

    fun signOutUser() {
        firebaseInstanceAuth.signOut()
    }

    fun signUp(context: Context, email: EditText, password: EditText, confirmPassword: EditText) {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()
        val confirmText = confirmPassword.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(context, "Input field cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            firebaseInstanceAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Successfully signed up", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Email already exists or badly formatted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (error: FirebaseAuthException) {
            Log.d(TAG, "[LOG] signUp(): ${error.message}")
        }
    }

    fun signIn(context: Context, email: EditText, password: EditText) {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(context, "Email or password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            firebaseInstanceAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Successfully logged in", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Wrong email or password", Toast.LENGTH_SHORT)
                            .show()
                    }

                    if (task.exception is FirebaseTooManyRequestsException) {
                        Toast.makeText(
                            context, "Too many attempts, please try again later", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (error: FirebaseAuthException) {
            Log.d(TAG, "[LOG ]signIn(): ${error.message}")
            Toast.makeText(context, "An error has occurred", Toast.LENGTH_SHORT).show()
        }
    }
}
