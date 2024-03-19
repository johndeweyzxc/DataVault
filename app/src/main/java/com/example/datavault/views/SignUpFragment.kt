package com.example.datavault.views

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.datavault.AuthActivity
import com.johndeweydev.datavault.R
import com.johndeweydev.datavault.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setClickListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setClickListeners() {
        binding.registerToolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
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
            (activity as AuthActivity).signUpWithEmail(
                binding.etRegisterEmail, binding.ilRegisterEmail, binding.etRegisterPassword,
                binding.ilRegisterPassword, binding.etRegisterConfirmPassword,
                binding.ilRegisterConfirmPassword
            )
        }

        binding.tvRegisterLoginInstead.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}