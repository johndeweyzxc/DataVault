package com.example.datavault

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.datavault.adapters.RvHome
import com.example.datavault.schema.SeedSchema
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser

interface MainInterface {
    fun currentUserEmail(): String
    fun currentUserName(): String
    fun currentUserUrlPhoto(): String
    fun homeAdapter(): RvHome
    fun adapterItemCount(): Int

    fun userMightBeNull(user: FirebaseUser?)

    fun updateData(
        firestoreDocId: String, appName: String, userName: String, email: String, password: String,
        phoneNumber: String, docId: String, createdAt: Timestamp, updatedAt: Timestamp
    )

    fun uploadData(
        appName: String, userName: String, email: String, password: String,
        phoneNumber: String, docId: String, createdAt: Timestamp, updatedAt: Timestamp
    )

    suspend fun fetchData(
        firestoreDocId: String, appName: TextInputEditText, userName: TextInputEditText,
        email: TextInputEditText, password: TextInputEditText, pNumber: TextInputEditText,
    ): List<Any>

    fun deleteData(context: Context, dataContent: SeedSchema)

    fun displayStatusToUser(context: Context, taskVoid: Task<Void>) {
        taskVoid.addOnSuccessListener {
            Toast.makeText(context, "Successfully deleted the data", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(context, "Canceled deleting data", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show()
        }
    }
}