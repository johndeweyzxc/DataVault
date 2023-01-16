package com.example.datavault.auth

import android.content.ContentValues
import android.util.Log
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignInWIthEmail {

    fun signUp(
        email: TextInputEditText, emailParent: TextInputLayout,
        password: TextInputEditText, passwordParent: TextInputLayout,
        confirmPassword: TextInputEditText, confirmParent: TextInputLayout
    ) {
        emailParent.helperText = null
        passwordParent.helperText = null
        confirmParent.helperText = null

        val emailText = email.text.toString()
        val passwordText = password.text.toString()
        val confirmText = confirmPassword.text.toString()

        when {
            emailText.isEmpty() -> {
                emailParent.helperText = "*Email cannot be empty"; return
            }
            passwordText.isEmpty() -> {
                passwordParent.helperText = "*Password cannot be empty"; return
            }
            confirmText.isEmpty() -> {
                confirmParent.helperText = "*Email cannot be empty"; return
            }
            passwordText != confirmText -> {
                confirmParent.helperText = "*Password did not match"; return
            }
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signIn(email, null, password, null)
                    email.text?.clear(); password.text?.clear(); confirmPassword.text?.clear()
                } else {
                    Log.d(ContentValues.TAG, "[LOG] signUp(): ${task.exception}")
                }
            }
    }

    fun signIn(email: TextInputEditText, emailParent: TextInputLayout?,
               password: TextInputEditText, passwordParent: TextInputLayout?) {

        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (passwordParent != null || emailParent != null) {
            emailParent?.helperText = null
            passwordParent?.helperText = null

            when {
                emailText.isEmpty() -> {
                    emailParent?.helperText = "*Email cannot be empty"; return
                }
                passwordText.isEmpty() -> {
                    passwordParent?.helperText = "*Password cannot be empty"; return
                }

            }
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    email.text?.clear(); password.text?.clear()
                } else {
                    Log.d(ContentValues.TAG, "[LOG] signIn(): ${task.exception}")
                }
            }
    }
}