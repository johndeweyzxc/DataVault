package com.example.datavault

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.datavault.auth.FirebaseAuthWithGoogle
import com.example.datavault.fragments.LoginFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // This is the listener whenever the user presses back button
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Set the firebase and the current user if there is a logged in user.
        setFirebaseAuth()
        // Set the authentication listener to check if a user just logged in.
        setAuthListener()
        // Configure google sign in so user can sign in with their google accounts.
        configureGoogleSignIn()
        // Set the first fragment to be shown.
        loginPageFragment()
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

    private fun loginPageFragment() {
        supportFragmentManager.beginTransaction().apply {
            // Replace the frame layout with login page fragment in the auth activity.
            replace(R.id.frameLayoutActivityAuth, LoginFragment(googleSignInClient, activityResultLauncher))
            commit()
        }
    }

    private fun setFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
    }

    private fun setAuthListener() {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
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
                FirebaseAuthWithGoogle(account.idToken!!, this).signIn()
            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "Failed getting result from google sign in",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}