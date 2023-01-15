package com.example.datavault.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange

class DataModelConverter {
    // Converts the documents retrieve from firestore to DataModel.
    fun convertToModel(changes: DocumentChange): DataModelRetrieve {
        return DataModelRetrieve(
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