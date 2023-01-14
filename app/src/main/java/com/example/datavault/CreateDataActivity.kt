package com.example.datavault

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.databinding.ActivityCreateDataBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class CreateDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateDataBinding

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
        binding = ActivityCreateDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the reference of views from the layout.
        bindViews()
        // After setting the reference of the views, set up click listeners on the view.
        setClickListenersOnViews()
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
        val toolBarBackButton: MaterialToolbar = binding.createToolBar
        val backButton: Button = binding.createBackButton

        ilAppName.setEndIconOnClickListener { binding.createEtAppname.text?.clear() }
        ilUserName.setEndIconOnClickListener { binding.createEtUsername.text?.clear() }
        ilEmail.setEndIconOnClickListener { binding.createEtEmail.text?.clear() }
        ilPhoneNumber.setEndIconOnClickListener { binding.createEtPassword.text?.clear() }

        toolBarBackButton.setOnClickListener { finish() }
        backButton.setOnClickListener { finish() }

        saveDataButton.setOnClickListener {
            clearHelperText()
            if (checkForBlackOrNull() == -1) { return@setOnClickListener; }
            closeActiveKeyboard()
            uploadData()
            clearEditText()
            finish()
        }
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

    private fun uploadData() {
        val collectionRef: CollectionReference = Firebase.firestore.collection("generatedUserData")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("data")

        val dataItem = DataModelUpload(
            etAppName.text.toString(),
            etUserName.text.toString(),
            etEmail.text.toString(),
            etPassword.text.toString(),
            etPhoneNumber.text.toString(),
            UUID.randomUUID().toString(),
            Timestamp(Date()),
            Timestamp(Date()),
        )

        collectionRef.add(dataItem).addOnSuccessListener {
            Toast.makeText(this@CreateDataActivity, "Successfully saved the data.", Toast.LENGTH_LONG)
                .show()
            }
            .addOnCanceledListener {
                Toast.makeText(this@CreateDataActivity, "Canceled saving data", Toast.LENGTH_LONG)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@CreateDataActivity, "Failed to save data", Toast.LENGTH_LONG)
                    .show()
                }
    }

    private fun closeActiveKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
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