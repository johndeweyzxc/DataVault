package com.example.datavault

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignInWithEmail {

    private var firebaseInstanceAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signOutUser() {
        firebaseInstanceAuth.signOut()
    }

    private fun showToast(context: Context, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun signUp(context: Context, email: EditText, password: EditText, confirmPassword: EditText) {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()
        val confirmText = confirmPassword.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            showToast(context, "Some input fields are empty")
            return
        }

        email.text.clear()
        password.text.clear()
        confirmPassword.text.clear()

        firebaseInstanceAuth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(context, "Successfully signed up")
                } else {
                    Log.d(TAG, "[LOG] signUp(): ${task.exception}")
                    showToast(context, task.exception?.message)
                }
            }
    }

    fun signIn(context: Context, email: EditText, password: EditText) {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            showToast(context, "Email and password cannot be empty")
            return
        }

        email.text.clear()
        password.text.clear()

        firebaseInstanceAuth.signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(context, "Successfully logged in")
                } else {
                    Log.d(TAG, "[LOG] signIn(): ${task.exception}")
                    showToast(context, task.exception?.message)
                }
            }
    }
}
