package com.example.datavault

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.datavault.databinding.FragmentDialogCreateBinding
import com.example.datavault.databinding.FragmentDialogEditBinding
import com.example.datavault.databinding.FragmentUserProfileBinding
import com.example.datavault.schema.SeedSchema
import com.example.datavault.schema.SeedSchemaUpload
import com.example.datavault.schema.UserProfile
import com.example.datavault.views.EditSeedDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

interface Database {

    private fun generatedUserDataColRef(): CollectionReference {
        return Firebase.firestore.collection("generatedUserData")
    }

    fun uploadData(context: Context, binding: FragmentDialogCreateBinding) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val dataModel = SeedSchemaUpload(
            binding.createEtAppname.text.toString(), binding.createEtUsername.text.toString(),
            binding.createEtEmail.text.toString(), binding.createEtPassword.text.toString(),
            binding.createEtPhonenumber.text.toString(), false,
            Timestamp(Date()), Timestamp(Date())
        )

        dataColRef.add(dataModel).addOnSuccessListener {
            Toast.makeText(context, "Successfully saved the data", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(context, "Canceled saving data", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_LONG).show()
        }
    }

    fun updateData(context: Context, seedData: SeedSchema, binding: FragmentDialogEditBinding) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(seedData.fireStoreDocId)
        val dataModel = SeedSchemaUpload(
            binding.editEtAppname.text.toString(), binding.editEtUsername.text.toString(),
            binding.editEtEmail.text.toString(), binding.editEtPassword.text.toString(),
            binding.editEtPhonenumber.text.toString(), seedData.favorite,
            seedData.createdAt, Timestamp(Date())
        )

        val task = targetDocument.set(dataModel)
        task.addOnSuccessListener {
            Toast.makeText(context, "Successfully updated the data", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(context, "Canceled updating the data", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to update the data", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteData(context: Context, seedData: SeedSchema, fragment: EditSeedDialog) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(seedData.fireStoreDocId)

        MaterialAlertDialogBuilder(context)
            .setTitle("Deleting ${seedData.appName}")
            .setMessage("Do you really want to delete ${seedData.appName}?")
            .setNeutralButton("CANCEL") { dialog, _ ->
                dialog.cancel()
                return@setNeutralButton
            }
            .setPositiveButton("DELETE") {_, _ ->
                val task = targetDocument.delete()
                task.addOnSuccessListener {
                    Toast.makeText(context, "Successfully deleted the data", Toast.LENGTH_SHORT).show()
                }.addOnCanceledListener {
                    Toast.makeText(context, "Canceled deleting data", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    if (exception.message != null) {
                        Log.i("devlog", exception.message!!)
                    }
                    Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show()
                }
                fragment.dismiss()
                return@setPositiveButton
            }.show()
    }

    fun addToFavorites(context: Context, fireStoreDocId: String, value: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("data")
        val targetDocument = dataColRef.document(fireStoreDocId)

        targetDocument.update("favorite", value).addOnSuccessListener {
            if (!value) return@addOnSuccessListener

            Toast.makeText(context, "Added to favorites", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(context, "Canceled saving to favorite", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_LONG).show()
        }
    }

    fun uploadInitialUserProfileData(context: Context, name: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("profile")
        val userProfile = UserProfile(name, "testing", Timestamp(Date()), Timestamp(Date()))
        val task = dataColRef.document("user").set(userProfile)

        task.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_LONG).show()
        }
    }

    fun updateUserProfileData(context: Context, binding: FragmentUserProfileBinding) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("profile")
        val userProfile = UserProfile(
            binding.etUserProfileName.text.toString(), "testing",
            Timestamp(Date()), Timestamp(Date())
        )
        val task = dataColRef.document("user").set(userProfile)

        task.addOnSuccessListener {
            Toast.makeText(context, "Successfully saved changes", Toast.LENGTH_LONG).show()
        }.addOnCanceledListener {
            Toast.makeText(context, "Canceled saving data", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.i("devlog", exception.message!!)
            }
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_LONG).show()
        }
    }
}