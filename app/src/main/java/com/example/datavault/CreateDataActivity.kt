package com.example.datavault

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.datavault.databinding.ActivityCreateDataBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class CreateDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateDataBinding
    private val dataVaultRef = Firebase.firestore.collection("dataVault")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createIlAppname.setEndIconOnClickListener { binding.createEtAppname.text?.clear() }
        binding.createIlUsername.setEndIconOnClickListener { binding.createEtUsername.text?.clear() }
        binding.createIlEmail.setEndIconOnClickListener { binding.createEtEmail.text?.clear() }
        binding.createIlPhonenumber.setEndIconOnClickListener { binding.createEtPassword.text?.clear() }

        binding.createToolBar.setOnClickListener { finish() }
        binding.createBackButton.setOnClickListener { finish() }

        binding.createSaveDataButton.setOnClickListener {
            binding.createIlAppname.helperText = null
            binding.createIlUsername.helperText = null
            binding.createIlEmail.helperText = null
            binding.createIlPhonenumber.helperText = null
            binding.createIlPassword.helperText = null
            if (checkForBlackOrNull() == -1) { return@setOnClickListener; }
            uploadData()
        }
    }

    private fun checkForBlackOrNull(): Int {
        val appName: EditText = binding.createEtAppname
        val userName: EditText = binding.createEtUsername
        val email: EditText = binding.createEtEmail
        val phoneNumber: EditText = binding.createEtPhonenumber
        val password: EditText = binding.createEtPassword

        when {
            appName.text.isNullOrBlank() -> {binding.createIlAppname.helperText = "*App name is required"; return -1 }
            userName.text.isNullOrBlank() -> {binding.createIlUsername.helperText = "*User name is required"; return -1}
            email.text.isNullOrBlank() -> {binding.createIlEmail.helperText = "*Email is required"; return -1}
            phoneNumber.text.isNullOrBlank() -> {
                binding.createIlPhonenumber.helperText = "*Phone number is required"; return -1
            }
            password.text.isNullOrBlank() -> {binding.createIlPassword.helperText = "Password is required"; return -1}
        }

        return 0
    }

    private fun uploadData() = CoroutineScope(Dispatchers.IO).launch {
        val dataItem = DataModelUpload(
            binding.createEtAppname.text.toString(),
            binding.createEtUsername.text.toString(),
            binding.createEtEmail.text.toString(),
            binding.createEtPassword.text.toString(),
            binding.createEtPhonenumber.text.toString(),
            UUID.randomUUID().toString(),
            Timestamp(Date()),
            Timestamp(Date()),
        )

        try {
            dataVaultRef.add(dataItem).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateDataActivity,
                    "Successfully saved data.", Toast.LENGTH_LONG).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateDataActivity,
                    e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}