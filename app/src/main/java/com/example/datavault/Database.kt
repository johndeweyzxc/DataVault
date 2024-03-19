package com.example.datavault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.datavault.models.MainViewModel
import com.example.datavault.schema.SeedSchema
import com.example.datavault.schema.SeedSchemaUpload
import com.example.datavault.schema.UserProfile
import com.example.datavault.views.EditSeedDialog
import com.example.datavault.views.MainFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.johndeweydev.datavault.databinding.FragmentDialogCreateBinding
import com.johndeweydev.datavault.databinding.FragmentDialogEditBinding
import com.johndeweydev.datavault.databinding.FragmentUserProfileBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
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
        val userProfile = UserProfile(name, Timestamp(Date()), Timestamp(Date()))
        val targetDocument = dataColRef.document("user")

        // Before uploading initial user data, check if it exists
        targetDocument.get().addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.d("devlog", "[uploadInitialUserProfileData()] ${exception.message}")
            }
        }

        val task = targetDocument.set(userProfile)

        task.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.w("devlog", "[uploadInitialUserProfileData()] ${exception.message}")
            }
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {
            Log.d("devlog",
                "[uploadInitialUserProfileData()] Successfully saved INITIAL user information in firestore.")
        }
    }

    fun updateUserProfileData(context: Context, binding: FragmentUserProfileBinding, viewModel: MainViewModel, uid: String) {
        val storageRef = Firebase.storage.reference
        val userProfileImageRef = storageRef.child("ProfileImage/${uid}.jpg")

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userIdDocRef = generatedUserDataColRef().document(currentUser!!.uid)
        val dataColRef = userIdDocRef.collection("profile")
        val userProfile = UserProfile(binding.etUserProfileName.text.toString(), Timestamp(Date()), Timestamp(Date())
        )
        val task = dataColRef.document("user").set(userProfile)

        task.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.w("devlog", "[updateUserProfileData()] ${exception.message}")
            }
            Toast.makeText(context, "Failed to save changes", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {
            Log.d("devlog", "[updateUserProfileData()] Successfully saved new user information in firestore.")
        }

        val bitmap = (binding.ivUserProfileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = userProfileImageRef.putBytes(data)

        uploadTask.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.w("devlog", "[updateUserProfileData()] ${exception.message}")
            }
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { taskSnapshot ->
            Log.d(
                "devlog",
                "[updateUserProfileData()] Successfully saved new user profile image in storage " +
                        "(${taskSnapshot.bytesTransferred})"
            )
        }
    }

    fun overrideDefaultUserProfileImage(fragment: MainFragment, imageView: CircleImageView, uid: String) {
        val storageRef = Firebase.storage.reference
        val userProfileImageRef = storageRef.child("ProfileImage/${uid}.jpg")

        userProfileImageRef.downloadUrl.addOnFailureListener { exception ->
            if (exception.message != null) {
                Log.w("devlog", "[overrideDefaultUserProfileImage()] USER PROFILE IMAGE NOT FOUND IN STORAGE.")
            }
        }.addOnSuccessListener { uri ->
            Log.i("devlog", "[overrideDefaultUserProfileImage()] Successfully downloaded the profile image from storage.")

            // Successfully downloaded the image, update the UI
            Glide.with(fragment).load(uri).into(imageView)
        }
    }
}