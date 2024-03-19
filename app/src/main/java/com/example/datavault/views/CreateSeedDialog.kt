package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.johndeweydev.datavault.databinding.FragmentDialogCreateBinding

class CreateSeedDialog : DialogFragment(), Database {

    private lateinit var binding: FragmentDialogCreateBinding

    private lateinit var etAppName: EditText
    private lateinit var etUserName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhoneNumber: EditText

    private lateinit var ilAppName: TextInputLayout
    private lateinit var ilUserName: TextInputLayout
    private lateinit var ilEmail: TextInputLayout
    private lateinit var ilPassword: TextInputLayout
    private lateinit var ilPhoneNumber: TextInputLayout

    private lateinit var saveDataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        // When this fragment enter, slide to the left
        enterTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
        // When this fragment exit, slide to the right
        exitTransition = inflater.inflateTransition(R.transition.fragment_slide_right)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDialogCreateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews()
        setClickListenersOnViews()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun bindViews() {
        etAppName = binding.createEtAppname
        etUserName = binding.createEtUsername
        etEmail = binding.createEtEmail
        etPhoneNumber = binding.createEtPhonenumber
        etPassword = binding.createEtPassword

        ilAppName = binding.createIlAppname
        ilUserName = binding.createIlUsername
        ilEmail = binding.createIlEmail
        ilPhoneNumber = binding.createIlPhonenumber
        ilPassword = binding.createIlPassword

        saveDataButton = binding.createSaveDataButton
    }

    private fun setClickListenersOnViews() {
        ilAppName.setEndIconOnClickListener { binding.createEtAppname.text?.clear() }
        ilUserName.setEndIconOnClickListener { binding.createEtUsername.text?.clear() }
        ilEmail.setEndIconOnClickListener { binding.createEtEmail.text?.clear() }
        ilPhoneNumber.setEndIconOnClickListener { binding.createEtPassword.text?.clear() }

        val scope = CoroutineScope(Dispatchers.IO)

        binding.createEditToolBar.setOnClickListener {
            closeActiveKeyboard()
            scope.launch {
                delay(200)
                dismiss()
            }
        }
        binding.createBackButton.setOnClickListener {
            closeActiveKeyboard()
            scope.launch {
                delay(200)
                dismiss()
            }
        }

        saveDataButton.setOnClickListener {
            clearHelperText()
            closeActiveKeyboard()

            if (checkForBlankOrNull() == -1) { return@setOnClickListener; }

            val mainActivity = (activity as MainActivity)
            if (mainActivity.currentUser == null) { mainActivity.navigateToAuthActivity() }
            uploadData(requireActivity(), binding)

            clearEditText()
            dismiss()
        }
    }

    private fun closeActiveKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun checkForBlankOrNull(): Int {
        when {
            etAppName.text.isNullOrBlank() -> {ilAppName.helperText = "*App name is required"; return -1 }
            etUserName.text.isNullOrBlank() -> {ilUserName.helperText = "*User name is required"; return -1}
            etEmail.text.isNullOrBlank() -> {ilEmail.helperText = "*Email is required"; return -1}
            etPhoneNumber.text.isNullOrBlank() -> {ilPhoneNumber.helperText = "*Phone number is required"; return -1 }
            etPassword.text.isNullOrBlank() -> {ilPassword.helperText = "Password is required"; return -1}
        }
        return 0
    }

    private fun clearEditText() {
        binding.createEtAppname.text!!.clear()
        binding.createEtEmail.text!!.clear()
        binding.createEtUsername.text!!.clear()
        binding.createEtPassword.text!!.clear()
        binding.createEtPhonenumber.text!!.clear()
    }

    private fun clearHelperText() {
        ilAppName.helperText = null
        ilUserName.helperText = null
        ilEmail.helperText = null
        ilPhoneNumber.helperText = null
        ilPassword.helperText = null
    }
}