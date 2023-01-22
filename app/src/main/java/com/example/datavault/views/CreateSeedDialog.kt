package com.example.datavault.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.datavault.MainActivity
import com.example.datavault.databinding.FragmentDialogCreateBinding
import com.example.datavault.models.SeedSchemaUpload
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class CreateSeedDialog : DialogFragment() {

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

        binding.createEditToolBar.setOnClickListener { dismiss() }
        binding.createBackButton.setOnClickListener { dismiss() }

        saveDataButton.setOnClickListener {
            clearHelperText()
            if (checkForBlankOrNull() == -1) { return@setOnClickListener; }
            closeActiveKeyboard()
            uploadData()
            clearEditText()
        }
    }

    private fun uploadData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        (activity as MainActivity).userMightBeNull(currentUser)

        val generatedUserData = Firebase.firestore.collection("generatedUserData")
        val userId = generatedUserData.document(currentUser!!.uid)
        val data = userId.collection("data")

        val dataItem = SeedSchemaUpload(
            etAppName.text.toString(),
            etUserName.text.toString(),
            etEmail.text.toString(),
            etPassword.text.toString(),
            etPhoneNumber.text.toString(),
            UUID.randomUUID().toString(),
            Timestamp(Date()),
            Timestamp(Date()),
        )

        val upload = data.add(dataItem)

        upload.addOnSuccessListener {
            Toast.makeText(requireActivity(), "Successfully saved the data.", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(requireActivity(), "Canceled saving data", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(requireActivity(), "Failed to save data", Toast.LENGTH_LONG).show()
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