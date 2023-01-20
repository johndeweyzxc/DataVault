package com.example.datavault.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.datavault.AuthActivity
import com.example.datavault.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

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
            // Tell fragment manager to pop this fragment from the back stack.
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
            // Tell fragment manager to pop this fragment from the back stack.
            parentFragmentManager.popBackStack()
        }
    }
}