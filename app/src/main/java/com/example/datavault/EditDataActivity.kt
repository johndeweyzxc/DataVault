package com.example.datavault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.databinding.ActivityEditDataBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDataBinding
    private lateinit var dataVaultRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editToolBar.setOnClickListener { finish() }
        binding.editBackButton.setOnClickListener { finish() }

        // Get the content of the document passed from data adapter
        val dataId: String? = intent.getStringExtra("fireStoreDocId")
        // The doc id is not the docId field in the document but the id provided by firestore.
        val dataAppName: String? = intent.getStringExtra("appName")
        val dataUserName: String? = intent.getStringExtra("userName")
        val dataEmail: String? = intent.getStringExtra("email")
        val dataPassword: String? = intent.getStringExtra("password")
        val dataPhoneNumber: String? = intent.getStringExtra("phoneNumber")

        // Get reference of the document in firebase via document id
        dataVaultRef = Firebase.firestore.collection("dataVault")
            .document(dataId as String)

        val ilAppName = binding.editIlAppname
        val ilUserName = binding.editIlUsername
        val ilEmail = binding.editIlEmail
        val ilPhoneNumber = binding.editIlPhonenumber
        val ilPassword = binding.editIlPassword

        val etAppName = binding.editEtAppname
        val etUserName = binding.editEtUsername
        val etEmail = binding.editEtEmail
        val etPhoneNumber = binding.editEtPhonenumber
        val etPassword = binding.editEtPassword

        // Set the values for the edit text
        etAppName.setText(dataAppName)
        etUserName.setText(dataUserName)
        etEmail.setText(dataEmail)
        etPassword.setText(dataPassword)
        etPhoneNumber.setText(dataPhoneNumber)

        ilAppName.setEndIconOnClickListener { etAppName.text?.clear() }
        ilUserName.setEndIconOnClickListener { etUserName.text?.clear() }
        ilEmail.setEndIconOnClickListener { etEmail.text?.clear() }
        ilPhoneNumber.setEndIconOnClickListener { etPhoneNumber.text?.clear() }
    }

    private fun updateData() {

    }
}