package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private var firebaseInstanceAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    private fun updateUI(user: FirebaseUser?) {
        // Checks if user is already logged in with google
        if (user != null) {
            val intent = Intent(applicationContext, ContentActivity::class.java)
            val userEmail = user.email
            val userName = user.displayName
            intent.putExtra("email", userEmail)
            intent.putExtra("name", userName)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        setContentView(R.layout.activity_main)
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val contentActivity = Intent(this, ContentActivity::class.java)
        val registerActivity = Intent(this, RegisterActivity::class.java)

        firebaseInstanceAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User just signed in
                contentActivity.putExtra("name", user.displayName)
                contentActivity.putExtra("email", user.email)
                startActivity(contentActivity)
            }
        }

        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val tvLoginForgotPassword = findViewById<TextView>(R.id.tvLoginForgotPassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val tvLoginSignUp = findViewById<TextView>(R.id.tvLoginSignUp)
        val btnLoginWithGoogle = findViewById<AppCompatButton>(R.id.btnLoginWithGoogle)

        btnLogin.setOnClickListener {
            SignInWithEmail().signIn(this, etLoginEmail, etLoginPassword)
        }

        btnLoginWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        tvLoginSignUp.setOnClickListener {
            startActivity(registerActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseInstanceAuth.currentUser
        updateUI(currentUser)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseInstanceAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = firebaseInstanceAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    companion object {
        const val TAG = "GoogleActivity"
        const val RC_SIGN_IN = 9001
    }
}