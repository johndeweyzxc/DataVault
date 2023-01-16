package com.example.datavault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datavault.auth.SignInWIthEmail
import com.example.datavault.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val firebaseInstance: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAuthenticationListener()

        binding.registerToolBar.setNavigationOnClickListener {
            finish()
        }

        binding.ilRegisterEmail.setEndIconOnClickListener { binding.etRegisterEmail.text?.clear() }

        binding.etRegisterEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.ilRegisterEmail.helperText?.isNotEmpty() == true) {
                binding.ilRegisterEmail.helperText = null
            }
        }

        binding.etRegisterPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.ilRegisterPassword.helperText?.isNotEmpty() == true) {
                binding.ilRegisterPassword.helperText = null
            }
        }

        binding.etRegisterConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.ilRegisterConfirmPassword.helperText?.isNotEmpty() == true) {
                binding.ilRegisterConfirmPassword.helperText = null
            }
        }

        binding.btnRegisterAccount.setOnClickListener {
            SignInWIthEmail().signUp(
                binding.etRegisterEmail, binding.ilRegisterEmail, binding.etRegisterPassword,
                binding.ilRegisterPassword, binding.etRegisterConfirmPassword,
                binding.ilRegisterConfirmPassword
            )
        }

        binding.tvRegisterLoginInstead.setOnClickListener {
            finish()
        }
    }

    private fun setupAuthenticationListener() {
        firebaseInstance.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User just created a new account
                startActivity(Intent(this, ContentActivity::class.java))
            }
        }
    }
}