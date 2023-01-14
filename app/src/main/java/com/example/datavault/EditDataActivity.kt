package com.example.datavault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.databinding.ActivityEditDataBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDataBinding
    private lateinit var documentRef: DocumentReference

    private lateinit var ilAppName: TextInputLayout
    private lateinit var ilUserName: TextInputLayout
    private lateinit var ilEmail: TextInputLayout
    private lateinit var ilPassword: TextInputLayout
    private lateinit var ilPhoneNumber: TextInputLayout

    private lateinit var etAppName: TextInputEditText
    private lateinit var etUserName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editToolBar.setOnClickListener { finish() }
        binding.editBackButton.setOnClickListener { finish() }

        val fireStoreDocId = intent.getStringExtra("fireStoreDocId") as String
        documentRef = Firebase.firestore.collection("generatedUserData")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("data")
            .document(fireStoreDocId)

        // Set the reference of edit text and input layouts from the view.
        referenceEditTextsAndInputLayouts()
        // Set the end icon click listeners.
        inputEndIconListener()
        // This set the default value of the document to input edit text.
        setDefaultValues()

        binding.editSaveChangesButton.setOnClickListener {
            if (checkForBlackOrNull() == 0) {
                updateData()
            }
        }
    }

    private fun inputEndIconListener() {
        ilAppName.setEndIconOnClickListener { etAppName.text?.clear() }
        ilUserName.setEndIconOnClickListener { etUserName.text?.clear() }
        ilEmail.setEndIconOnClickListener { etEmail.text?.clear() }
        ilPhoneNumber.setEndIconOnClickListener { etPhoneNumber.text?.clear() }
    }

    private fun referenceEditTextsAndInputLayouts() {
        ilAppName = binding.editIlAppname
        ilUserName = binding.editIlUsername
        ilEmail = binding.editIlEmail
        ilPassword = binding.editIlPassword
        ilPhoneNumber = binding.editIlPhonenumber

        etAppName = binding.editEtAppname
        etUserName = binding.editEtUsername
        etEmail = binding.editEtEmail
        etPhoneNumber = binding.editEtPhonenumber
        etPassword = binding.editEtPassword
    }

    private fun setDefaultValues() {
        val dataAppName: String? = intent.getStringExtra("appName")
        val dataUserName: String? = intent.getStringExtra("userName")
        val dataEmail: String? = intent.getStringExtra("email")
        val dataPassword: String? = intent.getStringExtra("password")
        val dataPhoneNumber: String? = intent.getStringExtra("phoneNumber")

        etAppName.setText(dataAppName)
        etUserName.setText(dataUserName)
        etEmail.setText(dataEmail)
        etPassword.setText(dataPassword)
        etPhoneNumber.setText(dataPhoneNumber)

    }

    private fun checkForBlackOrNull(): Int {
        when {
            etAppName.text.isNullOrBlank() -> {ilAppName.helperText = "*App name is required"; return -1 }
            etUserName.text.isNullOrBlank() -> {ilUserName.helperText = "*User name is required"; return -1}
            etEmail.text.isNullOrBlank() -> {ilEmail.helperText = "*Email is required"; return -1}
            etPhoneNumber.text.isNullOrBlank() -> {ilPhoneNumber.helperText = "*Phone number is required"; return -1 }
            etPassword.text.isNullOrBlank() -> {ilPassword.helperText = "Password is required"; return -1}
        }

        return 0
    }

    private fun updateData() {

    }
}