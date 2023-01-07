package com.example.datavault

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding
    private var firebaseInstanceAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureGoogleSignIn()
        setupAuthenticationListener()

        binding.btnLogin.setOnClickListener {
            SignInWithEmail().signIn(binding.etLoginEmail, binding.ilLoginEmail,
                binding.etLoginPassword, binding.ilLoginPassword)
        }

        binding.btnLoginWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            activityResultLauncher.launch(signInIntent)
        }

        binding.tvLoginSignUp.setOnClickListener {
            startActivity( Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in and update UI accordingly.
        val currentUser = firebaseInstanceAuth.currentUser
        updateUI(currentUser)
    }

    private fun setupAuthenticationListener() {
        firebaseInstanceAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User just signed in
                val intent = Intent(this, ContentActivity::class.java)
                intent.putExtra("email", user.email)
                intent.putExtra("name", user.displayName)
                // Replace the stack with content activity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun configureGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up activity result launcher for getting result from an activiy
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { ActivityResult ->
            // Result returned from launching the intent from google sign in api
            if (ActivityResult.resultCode == RESULT_OK && ActivityResult.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(ActivityResult.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "Google sign in: Success")

                    // Google Sign In was successful, authenticate with Firebase
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in: Failed", e)
                    Toast.makeText(applicationContext,
                        "Failed getting result from google sign in", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Checks if user is already logged in with google
        if (user != null) {
            val intent = Intent(applicationContext, ContentActivity::class.java)
            val userEmail = user.email
            val userName = user.displayName
            intent.putExtra("email", userEmail)
            intent.putExtra("name", userName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseInstanceAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Firebase authentication with google: success")
                    val user = firebaseInstanceAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Firebase authentication with google: failure", task.exception)
                    Toast.makeText(applicationContext,
                        "Firebase authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}