package com.example.datavault

import com.example.datavault.adapters.RvHome
import com.example.datavault.models.SeedSchemaUpload
import com.google.firebase.auth.FirebaseUser

interface MainInterface {
    fun currentUserEmail(): String
    fun currentUserName(): String
    fun currentUserUrlPhoto(): String
    fun homeAdapter(): RvHome
    fun adapterItemCount(): Int
    fun updateData(userId: String, firestoreDocId: String, dataModel: SeedSchemaUpload)
    fun uploadData(userId: String, dataModel: SeedSchemaUpload)
    fun userMightBeNull(user: FirebaseUser?)
}