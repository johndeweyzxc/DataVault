package com.example.datavault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseInstance: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureGoogleSignIn()
        setupAuthenticationListener()
        setClickListeners()
    }

    override fun onStart() {
        super.onStart()
        updateUI(firebaseInstance.currentUser)
    }

    private fun configureGoogleSignIn() {
        firebaseInstance = FirebaseAuth.getInstance()
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
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Toast.makeText(applicationContext,
                        "Failed getting result from google sign in", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupAuthenticationListener() {
        firebaseInstance.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                val intent = Intent(this, ContentActivity::class.java)
                intent.putExtra("email", auth.currentUser!!.email)
                intent.putExtra("name", auth.currentUser!!.displayName)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setClickListeners() {
        val loginButton: Button = binding.btnLogin
        val loginWithGoogleButton: Button = binding.btnLoginWithGoogle
        val loginSignUpTextView: TextView = binding.tvLoginSignUp

        loginButton.setOnClickListener {
            SignInWithEmail().signIn(
                binding.etLoginEmail,
                binding.ilLoginEmail,
                binding.etLoginPassword,
                binding.ilLoginPassword,
            )
        }
        loginWithGoogleButton.setOnClickListener { activityResultLauncher.launch(googleSignInClient.signInIntent) }
        loginSignUpTextView.setOnClickListener { startActivity( Intent(this, RegisterActivity::class.java)) }
    }

    private fun updateUI(user: FirebaseUser?) {
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
        firebaseInstance.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(firebaseInstance.currentUser)
                    Toast.makeText(applicationContext, "Successfully signed in with google", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Failed signing in with google", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}