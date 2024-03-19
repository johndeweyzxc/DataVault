package com.example.datavault

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
import com.johndeweydev.datavault.R

class AuthActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        authListener()

        setContentView(R.layout.activity_auth)
        super.onCreate(savedInstanceState)
        Log.i("devlog", "[onCreate()] AuthActivity launched")

        configureGoogleSignIn()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        // Set the MainFragment as the first fragment to appear on the activity
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayoutActivityAuth, LoginFragment())
            commit()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // This handles the configuration change of the screen to prevent restart of MainActivity.
        Log.w("devlog", "[onConfigurationChanged()] Device configurate changed")
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.w("devlog",
                "[onConfigurationChanged()] Screen orientation changed to landscape in AuthActivity")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.w("devlog",
                "[onConfigurationChanged()] Screen orientation changed to portrait in AuthActivity")
        }
        super.onConfigurationChanged(newConfig)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        // If the user presses back button, pop the fragment stack or pause the activity
        // if there is only one fragment.
        override fun handleOnBackPressed() {
            val countStack = supportFragmentManager.backStackEntryCount
            val latestFragment = supportFragmentManager.getBackStackEntryAt(countStack - 1)

            Log.i("devlog", "[handleOnBackPressed()] Back button is pressed")
            if (countStack == 0) {
                moveTaskToBack(true)
            } else {
                supportFragmentManager.popBackStack()
            }
            Log.i("devlog",
                "[handleOnBackPressed()] Current fragment back stack count in MainActivity -> $countStack"
            )
            Log.i("devlog",
                "[handleOnBackPressed()] Current top of fragment stack in MainActivity " +
                        "-> ${latestFragment.name}"
            )
        }
    }

    private fun userNotNullUpdateUi(user: FirebaseUser?) {
        // If the current user is not null, then user is logged in. Navigate to MainActivity
        if (user != null) {
            Log.w("devlog", "[userMightBeNull()] User is not null, navigate to MainActivity")
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

        // Set up activity result launcher for getting result from an activity.
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
                authWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                if (e.message != null) {
                    Log.e("devlog", "[configureGoogleSignIn()] ERROR AUTHENTICATING WITH GOOGLE")
                    Log.e("devlog", e.message!!)
                }
                Toast.makeText(this, "There is a problem signing in with google", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun authWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("devlog", "[authWithGoogle()] Client successfully signed in with google")
            } else {
                Log.e("devlog", "[authWithGoogle()] ERROR AUTHENTICATING WITH GOOGLE IN FIREBASE")
                Toast.makeText(this, "There is a problem signing in with google", Toast.LENGTH_LONG).show()
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
                email.text?.clear()
                password.text?.clear()
            } else {
                if (task.exception?.message != null) {
                    Log.e("devlog", "[signInWithEmail()] ERROR SIGNING IN WITH EMAIL")
                    Log.e("devlog", "[signInWithEmail()] ${task.exception?.message!!}")
                }
                Toast.makeText(applicationContext, task.exception?.message!!, Toast.LENGTH_LONG).show()
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
                email.text?.clear()
                password.text?.clear()
                confirmPassword.text?.clear()
            } else {
                if (task.exception?.message != null) {
                    Log.e("devlog", "[signUpWithEmail()] ERROR SIGNING UP WITH EMAIL")
                    Log.e("devlog", task.exception?.message!!)
                }
                Toast.makeText(applicationContext, "There is a problem signing up with email", Toast.LENGTH_LONG).show()
            }
        }
    }
}