package com.example.datavault.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.datavault.AuthActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var authActivity: AuthActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        authActivity = (activity as AuthActivity)
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
        val privacyAndTerms: TextView = binding.textPrivacyAndTerms
        val forgotPassword: TextView = binding.tvLoginForgotPassword

        forgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityAuth, ForgotPasswordFragment(), "ForgotPasswordFragment")
                addToBackStack("ForgotPasswordFragment")
                commit()
            }
        }
        loginButton.setOnClickListener {
            (activity as AuthActivity).signInWithEmail(
                binding.etLoginEmail, binding.ilLoginEmail,
                binding.etLoginPassword, binding.ilLoginPassword,
            )
        }
        loginWithGoogleButton.setOnClickListener {
            authActivity.activityResultLauncher.launch(authActivity.googleSignInClient.signInIntent)
        }
        loginSignUpTextView.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityAuth, SignUpFragment(), "SignUpFragment")
                addToBackStack("SignUpFragment")
                commit()
            }
        }
        privacyAndTerms.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frameLayoutActivityAuth, AboutAppFragment(), "AboutAppFragment")
                addToBackStack("AboutAppFragment")
                commit()
            }
        }
    }
}