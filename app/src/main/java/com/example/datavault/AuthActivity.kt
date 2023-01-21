package com.example.datavault

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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        authListener()

        setContentView(R.layout.activity_auth)
        super.onCreate(savedInstanceState)

        configureGoogleSignIn()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        // Set the MainFragment as the first fragment to appear on the activity
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayoutActivityAuth, LoginFragment(googleSignInClient, activityResultLauncher))
            commit()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("devlog", "Screen orientation changed to landscape in AuthActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("devlog", "Screen orientation changed to portrait in AuthActivity")
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

    private fun userNotNullUpdateUi(user: FirebaseUser?) {
        if (user != null) {
            Log.i("devlog", "User is logged in, navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun authListener() {
        FirebaseAuth.getInstance().addAuthStateListener { auth -> userNotNullUpdateUi(auth.currentUser)}
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
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(ActivityResult.data)
                val account = task.getResult(ApiException::class.java)!!
                // Authenticate in firebase with google using the token
                authWithGoogle(account.idToken!!, this)
            } catch (e: ApiException) {
                if (e.message != null) {
                    Log.i("devlog", e.message!!)
                }
                Toast.makeText(this, "Failed getting result from google sign in", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun authWithGoogle(idToken: String, mainContext: Context) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(mainContext, "Successfully signed in with google", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mainContext, "Failed signing in with google", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signInWithEmail(
        email: TextInputEditText, emailParent: TextInputLayout?,
        password: TextInputEditText, passwordParent: TextInputLayout?
    ) {
        val emailText = email.text.toString()
        val passwordText = password.text.toString()

        if (passwordParent == null || emailParent == null) {
            return
        }

        emailParent.helperText = null
        passwordParent.helperText = null
        when {
            emailText.isEmpty() -> {
                emailParent.helperText = "*Email cannot be empty"; return
            }
            passwordText.isEmpty() -> {
                passwordParent.helperText = "*Password cannot be empty"; return
            }
        }

        val signIn = FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText, passwordText)

        signIn.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                email.text?.clear(); password.text?.clear()
            } else {
                if (task.exception != null) {
                    Log.i("devlog", task.exception.toString())
                }
                Toast.makeText(applicationContext, "There is a problem signing in", Toast.LENGTH_LONG).show()
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

        val signUp = FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailText, passwordText)

        signUp.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                email.text?.clear(); password.text?.clear(); confirmPassword.text?.clear()
            } else {
                if (task.exception != null) {
                    Log.i("devlog", task.exception.toString())
                }
                Toast.makeText(applicationContext, "There is a problem signing up", Toast.LENGTH_LONG).show()
            }
        }
    }
}