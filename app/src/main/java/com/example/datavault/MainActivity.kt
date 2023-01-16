package com.example.datavault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.auth.FirebaseAuthWithGoogle
import com.example.datavault.auth.SignInWIthEmail
import com.example.datavault.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseInstance: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the firebase instance and the current user if there is a logged in user.
        setFirebaseInstances()
        // Set the authentication listener to check if a user just logged in.
        setupAuthenticationListener()
        // Configure google sign in so user can sign in with their google accounts.
        configureGoogleSignIn()
        // Click listeners for buttons
        setClickListeners()
    }

    private fun setFirebaseInstances() {
        firebaseInstance = FirebaseAuth.getInstance()
        currentUser = firebaseInstance.currentUser
    }

    private fun setupAuthenticationListener() {
        firebaseInstance.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                val intent = Intent(this, ContentActivity::class.java)
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
                FirebaseAuthWithGoogle(account.idToken!!, applicationContext).signIn()
            } catch (e: ApiException) {
                Toast.makeText(
                    applicationContext,
                    "Failed getting result from google sign in",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setClickListeners() {
        val loginButton: Button = binding.btnLogin
        val loginWithGoogleButton: Button = binding.btnLoginWithGoogle
        val loginSignUpTextView: TextView = binding.tvLoginSignUp
        loginButton.setOnClickListener {
            SignInWIthEmail().signIn(
                binding.etLoginEmail, binding.ilLoginEmail,
                binding.etLoginPassword, binding.ilLoginPassword,
            )
        }
        loginWithGoogleButton.setOnClickListener {
            activityResultLauncher.launch(googleSignInClient.signInIntent)
        }
        loginSignUpTextView.setOnClickListener {
            startActivity( Intent(this, RegisterActivity::class.java))
        }
    }
}