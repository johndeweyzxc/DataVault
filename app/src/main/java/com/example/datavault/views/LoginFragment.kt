package com.example.datavault.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.datavault.AuthActivity
import com.example.datavault.R
import com.example.datavault.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class LoginFragment(private val googleSignInClient: GoogleSignInClient,
                    private val activityResultLauncher: ActivityResultLauncher<Intent>
) : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setClickListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setClickListeners() {
        val loginButton: Button = binding.btnLogin
        val loginWithGoogleButton: Button = binding.btnLoginWithGoogle
        val loginSignUpTextView: TextView = binding.tvLoginSignUp
        loginButton.setOnClickListener {
            (activity as AuthActivity).signInWithEmail(
                binding.etLoginEmail, binding.ilLoginEmail,
                binding.etLoginPassword, binding.ilLoginPassword,
            )
        }
        loginWithGoogleButton.setOnClickListener {
            activityResultLauncher.launch(googleSignInClient.signInIntent)
        }
        loginSignUpTextView.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityAuth, SignUpFragment(), "SignUpFragment")
                addToBackStack("SignUpFragment")
                commit()
            }
        }
    }
}