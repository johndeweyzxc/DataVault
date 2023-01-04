package com.example.datavault

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateDataActivity : AppCompatActivity() {

    private val dataVaultRef = Firebase.firestore.collection("dataVault")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_data)

        val createAppName = findViewById<EditText>(R.id.createAppName)
        val createUserName = findViewById<EditText>(R.id.createUserName)
        val createEmail = findViewById<EditText>(R.id.createEmail)
        val createPassword = findViewById<EditText>(R.id.createPassword)
        val createPhoneNumber = findViewById<EditText>(R.id.createPhoneNumber)
        val createButton = findViewById<AppCompatButton>(R.id.createButton)

        createButton.setOnClickListener {
            if (createAppName.text.isEmpty()) {
                Toast.makeText(
                    this, "App name must have a value!", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val dataItem = DataModel(
                createAppName.text.toString(),
                createUserName.text.toString(),
                createEmail.text.toString(),
                createPassword.text.toString(),
                createPhoneNumber.text.toString(),
                UUID.randomUUID().toString(),
            )
            uploadData(dataItem)
        }
    }

    private fun uploadData(dataModel: DataModel) = CoroutineScope(Dispatchers.IO).launch {
        try {
            dataVaultRef.add(dataModel).await()
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