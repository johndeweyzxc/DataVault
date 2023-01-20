package com.example.datavault

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.views.LoginFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // This is the listener whenever the user presses back button
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Set the authentication listener to check if a user just logged in.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        // Configure google sign in so user can sign in with their google accounts.
        configureGoogleSignIn()

        // Set the first fragment to be shown.
        supportFragmentManager.beginTransaction().apply {
            // Replace the frame layout with login page fragment in the auth activity.
            replace(R.id.frameLayoutActivityAuth, LoginFragment(googleSignInClient, activityResultLauncher))
            commit()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("DEV.LOG.INFO", "Screen orientation changed to landscape in AuthActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("DEV.LOG.INFO", "Screen orientation changed to portrait in AuthActivity")
        }
        super.onConfigurationChanged(newConfig)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up activity result launcher for getting result from an activiy.
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { ActivityResult ->
            // Result returned from launching the intent from google sign in api.
            if (ActivityResult.resultCode != RESULT_OK || ActivityResult.data == null) {
                return@registerForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(ActivityResult.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                authWithGoogle(account.idToken!!, this)
            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "Failed getting result from google sign in",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun authWithGoogle(idToken: String, mainContext: Context) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    mainContext, "Successfully signed in with google", Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(mainContext, "Failed signing in with google", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun signInWithEmail(email: TextInputEditText, emailParent: TextInputLayout?,
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

    fun signUpWithEmail(
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
                    email.text?.clear(); password.text?.clear(); confirmPassword.text?.clear()
                } else {
                    Log.d(ContentValues.TAG, "[LOG] signUp(): ${task.exception}")
                }
            }
    }
}