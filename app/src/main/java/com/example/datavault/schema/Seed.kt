package com.example.datavault.schema

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange

// For uploading data to the database
data class SeedSchemaUpload(
    var appName: String,
    var userName: String,
    var email: String,
    var password: String,
    var phoneNumber: String,
    var docId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)

// For retrieving data from the database
data class SeedSchema(
    var appName: String,
    var userName: String,
    var email: String,
    var password: String,
    var phoneNumber: String,
    var docId: String,
    var fireStoreDocId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)

class DataModelConverter {
    // Converts the documents retrieve from firestore to DataModel.
    fun convertToModel(changes: DocumentChange): SeedSchema {
        return SeedSchema(
            changes.document.get("appName") as String,
            changes.document.get("userName") as String,
            changes.document.get("email") as String,
            changes.document.get("password") as String,
            changes.document.get("phoneNumber") as String,
            changes.document.get("docId") as String,
            changes.document.id,
            changes.document.get("createdAt") as Timestamp?,
            changes.document.get("updatedAt") as Timestamp?,
        )
    }
}